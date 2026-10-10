package com.example.juntavecinosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.juntavecinosapp.data.repository.MovimientoRepository
import com.example.juntavecinosapp.model.Movimiento
import com.example.juntavecinosapp.model.TipoMovimiento
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovimientoFormState(
    val tipo: String = TipoMovimiento.PAGO,
    val concepto: String = "",
    val monto: String = "",          // texto, para poder editarlo en el TextField
    val fecha: Long = System.currentTimeMillis(),
    val persona: String = "",
    val descripcion: String = "",
    val uriComprobante: String? = null,

    val errorConcepto: String? = null,
    val errorMonto: String? = null,
    val errorFecha: String? = null,
    val errorPersona: String? = null,
    val errorDescripcion: String? = null,
    val errorComprobante: String? = null,

    val guardando: Boolean = false
) {
    val tieneErrores: Boolean
        get() = listOf(
            errorConcepto, errorMonto, errorFecha,
            errorPersona, errorDescripcion, errorComprobante
        ).any { it != null }
}

sealed interface EventoFormulario {
    data object Guardado : EventoFormulario
    data object Invalido : EventoFormulario
    data class Error(val mensaje: String) : EventoFormulario
}

class MovimientoViewModel(
    private val repository: MovimientoRepository
) : ViewModel() {

    // ======================= FORMULARIO =======================

    private val _form = MutableStateFlow(MovimientoFormState())
    val form: StateFlow<MovimientoFormState> = _form.asStateFlow()

    private val _eventos = Channel<EventoFormulario>(Channel.BUFFERED)
    val eventos: Flow<EventoFormulario> = _eventos.receiveAsFlow()

    fun iniciarFormulario(tipo: String) {
        _form.value = MovimientoFormState(tipo = tipo)
    }

    fun onTipoChange(tipo: String) {
        _form.update {
            it.copy(
                tipo = tipo,
                errorPersona = validarPersona(tipo, it.persona).takeIf { _ -> it.persona.isNotEmpty() },
                errorComprobante = if (it.errorComprobante != null) validarComprobante(tipo, it.uriComprobante) else null
            )
        }
    }

    fun onConceptoChange(valor: String) = _form.update {
        it.copy(concepto = valor, errorConcepto = validarConcepto(valor))
    }

    fun onMontoChange(valor: String) {
        val limpio = valor.filter { c -> c.isDigit() }.take(MAX_DIGITOS_MONTO)
        _form.update { it.copy(monto = limpio, errorMonto = validarMonto(limpio)) }
    }

    fun onFechaChange(valor: Long) = _form.update {
        it.copy(fecha = valor, errorFecha = validarFecha(valor))
    }

    fun onPersonaChange(valor: String) = _form.update {
        it.copy(persona = valor, errorPersona = validarPersona(it.tipo, valor))
    }

    fun onDescripcionChange(valor: String) = _form.update {
        it.copy(descripcion = valor, errorDescripcion = validarDescripcion(valor))
    }

    fun onComprobanteChange(uri: String?) = _form.update {
        it.copy(uriComprobante = uri, errorComprobante = validarComprobante(it.tipo, uri))
    }

    fun onComprobanteError(mensaje: String) = _form.update {
        it.copy(errorComprobante = mensaje)
    }

    fun guardar() {
        val actual = _form.value
        if (actual.guardando) return   // evita doble clic

        val validado = validarTodo(actual)
        _form.value = validado
        if (validado.tieneErrores) {
            _eventos.trySend(EventoFormulario.Invalido)
            return
        }

        viewModelScope.launch {
            _form.update { it.copy(guardando = true) }
            try {
                repository.insertar(
                    Movimiento(
                        tipo = validado.tipo,
                        concepto = validado.concepto.trim(),
                        monto = validado.monto.toLong(),
                        fecha = validado.fecha,
                        persona = validado.persona.trim(),
                        descripcion = validado.descripcion.trim(),
                        uriComprobante = validado.uriComprobante
                    )
                )
                _form.value = MovimientoFormState(tipo = validado.tipo)
                _eventos.send(EventoFormulario.Guardado)
            } catch (e: Exception) {
                _form.update { it.copy(guardando = false) }
                _eventos.send(EventoFormulario.Error("No se pudo guardar el movimiento. Intenta nuevamente."))
            }
        }
    }


    private val _filtro = MutableStateFlow<String?>(null)
    val filtro: StateFlow<String?> = _filtro.asStateFlow()

    fun filtrarPor(tipo: String?) {
        _filtro.value = tipo
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val movimientos: StateFlow<List<Movimiento>> = _filtro
        .flatMapLatest { tipo ->
            if (tipo == null) repository.movimientos
            else repository.movimientosPorTipo(tipo)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalEntradas: StateFlow<Long> = repository.totalEntradas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val totalSalidas: StateFlow<Long> = repository.totalSalidas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val balance: StateFlow<Long> = combine(totalEntradas, totalSalidas) { entradas, salidas ->
        entradas - salidas
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val totalPagos: StateFlow<Long> = repository.totalPorTipo(TipoMovimiento.PAGO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val totalIngresos: StateFlow<Long> = repository.totalPorTipo(TipoMovimiento.INGRESO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    /** Qué parte de lo recaudado ya se gastó, de 0.0 a 1.0 (para la barra de progreso). */
    val porcentajeGastado: StateFlow<Float> = combine(totalEntradas, totalSalidas) { entradas, salidas ->
        when {
            entradas <= 0L -> if (salidas > 0L) 1f else 0f
            else -> (salidas.toFloat() / entradas.toFloat()).coerceIn(0f, 1f)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    /** Los 5 gastos más recientes: lo que la comunidad quiere ver. */
    val ultimosGastos: StateFlow<List<Movimiento>> = repository.movimientosPorTipo(TipoMovimiento.GASTO)
        .map { lista -> lista.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun eliminar(movimiento: Movimiento) {
        viewModelScope.launch { repository.eliminar(movimiento) }
    }


    private fun validarTodo(s: MovimientoFormState) = s.copy(
        errorConcepto = validarConcepto(s.concepto),
        errorMonto = validarMonto(s.monto),
        errorFecha = validarFecha(s.fecha),
        errorPersona = validarPersona(s.tipo, s.persona),
        errorDescripcion = validarDescripcion(s.descripcion),
        errorComprobante = validarComprobante(s.tipo, s.uriComprobante)
    )

    private fun validarConcepto(valor: String): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "Ingresa el concepto"
            texto.length < 3 -> "El concepto debe tener al menos 3 caracteres"
            texto.length > 60 -> "El concepto no puede superar los 60 caracteres"
            else -> null
        }
    }

    private fun validarMonto(valor: String): String? {
        if (valor.isBlank()) return "Ingresa el monto"
        val numero = valor.toLongOrNull() ?: return "Ingresa un monto válido"
        return when {
            numero <= 0 -> "El monto debe ser mayor a 0"
            numero > MONTO_MAXIMO -> "El monto no puede superar los 100 millones"
            else -> null
        }
    }

    private fun validarFecha(valor: Long): String? =
        if (valor > System.currentTimeMillis()) "La fecha no puede ser futura" else null

    private fun validarPersona(tipo: String, valor: String): String? {
        val texto = valor.trim()
        return when {
            tipo == TipoMovimiento.INGRESO && texto.isEmpty() -> null
            texto.isEmpty() ->
                if (tipo == TipoMovimiento.GASTO) "Ingresa el proveedor" else "Ingresa el nombre del vecino"
            texto.length < 3 -> "Debe tener al menos 3 caracteres"
            texto.length > 50 -> "No puede superar los 50 caracteres"
            else -> null
        }
    }

    private fun validarDescripcion(valor: String): String? =
        if (valor.length > 200) "La descripción no puede superar los 200 caracteres" else null

    private fun validarComprobante(tipo: String, uri: String?): String? =
        if (comprobanteObligatorio(tipo) && uri.isNullOrBlank())
            "Adjunta una foto del comprobante"
        else null

    /** Regla de negocio acordada con Isa: obligatorio en todos los tipos. */
    private fun comprobanteObligatorio(tipo: String): Boolean = true

    private companion object {
        const val MAX_DIGITOS_MONTO = 9
        const val MONTO_MAXIMO = 100_000_000L
    }
}

class MovimientoViewModelFactory(
    private val repository: MovimientoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MovimientoViewModel::class.java))
        return MovimientoViewModel(repository) as T
    }
}