package com.example.juntavecinosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.juntavecinosapp.data.repository.ArriendoRepository
import com.example.juntavecinosapp.model.Arriendo
import com.example.juntavecinosapp.model.Dependencias
import com.example.juntavecinosapp.model.EstadoArriendo
import com.example.juntavecinosapp.model.RolUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

//Estado del formulario "Solicitar arriendo".
data class SolicitudFormState(
    val dependencia: String = "",
    val fecha: LocalDate? = null,
    val horaInicio: LocalTime? = null,
    val horaFin: LocalTime? = null,
    val motivo: String = "",

    val errorDependencia: String? = null,
    val errorFecha: String? = null,
    val errorHoraInicio: String? = null,
    val errorHoraFin: String? = null,
    val errorMotivo: String? = null,

    val cargando: Boolean = false
) {
    val tieneErrores: Boolean
        get() = listOf(
            errorDependencia, errorFecha, errorHoraInicio, errorHoraFin, errorMotivo
        ).any { it != null }

    //Monto según la tarifa y las horas elegidas; null mientras falten datos.
    val montoEstimado: Int?
        get() {
            val tarifa = Dependencias.tarifaPorHora(dependencia) ?: return null
            val inicio = horaInicio ?: return null
            val fin = horaFin ?: return null
            val minutos = Duration.between(inicio, fin).toMinutes()
            if (minutos <= 0) return null
            return (tarifa * minutos / 60).toInt()
        }
}

//Lógica de los arriendos: solicitar, listar y aprobar o rechazar.
class ArriendoViewModel(
    private val repository: ArriendoRepository
) : ViewModel() {

    // ======================= FORMULARIO =======================

    private val _form = MutableStateFlow(SolicitudFormState())
    val form: StateFlow<SolicitudFormState> = _form.asStateFlow()

    private val _eventos = Channel<EventoFormulario>(Channel.BUFFERED)
    val eventos: Flow<EventoFormulario> = _eventos.receiveAsFlow()

    fun iniciarFormulario() {
        _form.value = SolicitudFormState()
    }

    fun onDependenciaChange(valor: String) = _form.update {
        it.copy(dependencia = valor, errorDependencia = validarDependencia(valor))
    }

    fun onFechaChange(valor: LocalDate) = _form.update {
        it.copy(
            fecha = valor,
            errorFecha = validarFecha(valor),
            // si hoy es la fecha elegida, la hora de inicio ya puesta puede quedar inválida
            errorHoraInicio = if (it.horaInicio != null)
                validarHoraInicio(it.horaInicio, valor) else it.errorHoraInicio
        )
    }

    fun onHoraInicioChange(valor: LocalTime) = _form.update {
        it.copy(
            horaInicio = valor,
            errorHoraInicio = validarHoraInicio(valor, it.fecha),
            errorHoraFin = if (it.horaFin != null)
                validarHoraFin(valor, it.horaFin) else it.errorHoraFin
        )
    }

    fun onHoraFinChange(valor: LocalTime) = _form.update {
        it.copy(horaFin = valor, errorHoraFin = validarHoraFin(it.horaInicio, valor))
    }

    fun onMotivoChange(valor: String) = _form.update {
        it.copy(motivo = valor, errorMotivo = validarMotivo(valor))
    }

    //Valida todo, revisa que el horario esté libre y guarda la solicitud como PENDIENTE.
    fun solicitar(usuarioId: Int) {
        val actual = _form.value
        if (actual.cargando) return   // evita doble clic

        val validado = validarTodo(actual)
        _form.value = validado
        if (validado.tieneErrores) {
            _eventos.trySend(EventoFormulario.Invalido)
            return
        }

        val fecha = validado.fecha ?: return
        val horaInicio = validado.horaInicio ?: return
        val horaFin = validado.horaFin ?: return

        _form.update { it.copy(cargando = true) }
        viewModelScope.launch {
            try {
                // regla de negocio central del caso: no duplicar reservas
                if (repository.hayChoque(validado.dependencia, fecha, horaInicio, horaFin)) {
                    _form.update {
                        it.copy(
                            cargando = false,
                            errorHoraFin = "Ese horario ya está ocupado en esa dependencia"
                        )
                    }
                    _eventos.send(EventoFormulario.Invalido)
                    return@launch
                }

                repository.guardar(
                    Arriendo(
                        usuarioId = usuarioId,
                        dependencia = validado.dependencia,
                        fecha = fecha,
                        horaInicio = horaInicio,
                        horaFin = horaFin,
                        motivo = validado.motivo.trim(),
                        monto = validado.montoEstimado ?: 0
                    )
                )
                _form.value = SolicitudFormState()
                _eventos.send(EventoFormulario.Guardado)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _form.update { it.copy(cargando = false) }
                _eventos.send(EventoFormulario.Error("No se pudo guardar la solicitud. Intenta de nuevo."))
            }
        }
    }

    // ======================= LISTAS Y DETALLE =======================

    //Todos los arriendos (para la directiva y para ver disponibilidad).
    val todos: StateFlow<List<Arriendo>> = repository.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    //Solo los arriendos de un vecino.
    fun arriendosDe(usuarioId: Int): Flow<List<Arriendo>> =
        repository.observarPorUsuario(usuarioId)

    private val _detalle = MutableStateFlow<Arriendo?>(null)
    val detalle: StateFlow<Arriendo?> = _detalle.asStateFlow()

    fun cargarDetalle(id: Int) {
        viewModelScope.launch { _detalle.value = repository.buscarPorId(id) }
    }

    // ======================= APROBAR / RECHAZAR =======================

    /** Solo la directiva puede resolver una solicitud, y solo si sigue PENDIENTE. */
    fun resolverSolicitud(id: Int, nuevoEstado: String, rolUsuario: String) {
        if (rolUsuario != RolUsuario.DIRECTIVA) {
            _eventos.trySend(EventoFormulario.Error("Solo la directiva puede aprobar o rechazar"))
            return
        }
        if (nuevoEstado != EstadoArriendo.APROBADO && nuevoEstado != EstadoArriendo.RECHAZADO) {
            _eventos.trySend(EventoFormulario.Error("Estado no válido"))
            return
        }

        viewModelScope.launch {
            try {
                val arriendo = repository.buscarPorId(id)
                when {
                    arriendo == null ->
                        _eventos.send(EventoFormulario.Error("No se encontró la solicitud"))
                    arriendo.estado != EstadoArriendo.PENDIENTE ->
                        _eventos.send(EventoFormulario.Error("Esta solicitud ya fue resuelta"))
                    else -> {
                        repository.cambiarEstado(id, nuevoEstado)
                        _detalle.value = repository.buscarPorId(id)
                        _eventos.send(EventoFormulario.Guardado)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _eventos.send(EventoFormulario.Error("No se pudo actualizar la solicitud"))
            }
        }
    }

    // ======================= VALIDACIONES =======================
    // Funciones puras: devuelven el mensaje de error o null si está bien.

    private fun validarTodo(f: SolicitudFormState) = f.copy(
        errorDependencia = validarDependencia(f.dependencia),
        errorFecha = validarFecha(f.fecha),
        errorHoraInicio = validarHoraInicio(f.horaInicio, f.fecha),
        errorHoraFin = validarHoraFin(f.horaInicio, f.horaFin),
        errorMotivo = validarMotivo(f.motivo)
    )

    private fun validarDependencia(valor: String): String? =
        if (Dependencias.tarifaPorHora(valor) == null) "Elige una dependencia" else null

    private fun validarFecha(fecha: LocalDate?): String? {
        val hoy = LocalDate.now()
        return when {
            fecha == null -> "Elige una fecha"
            fecha.isBefore(hoy) -> "La fecha no puede ser pasada"
            fecha.isAfter(hoy.plusMonths(MESES_MAXIMOS)) ->
                "Solo se puede reservar con hasta 6 meses de anticipación"
            else -> null
        }
    }

    private fun validarHoraInicio(hora: LocalTime?, fecha: LocalDate?): String? = when {
        hora == null -> "Elige la hora de inicio"
        hora.isBefore(HORA_APERTURA) -> "La sede abre a las 08:00"
        !hora.isBefore(HORA_CIERRE) -> "La hora de inicio debe ser antes de las 22:00"
        fecha == LocalDate.now() && hora.isBefore(LocalTime.now()) ->
            "Esa hora ya pasó"
        else -> null
    }

    private fun validarHoraFin(inicio: LocalTime?, fin: LocalTime?): String? {
        if (fin == null) return "Elige la hora de término"
        if (fin.isAfter(HORA_CIERRE)) return "La sede cierra a las 22:00"
        if (inicio == null) return null   // se compara cuando elija el inicio

        val minutos = Duration.between(inicio, fin).toMinutes()
        return when {
            !fin.isAfter(inicio) -> "Debe ser posterior a la hora de inicio"
            minutos < MINUTOS_MINIMOS -> "El arriendo mínimo es de 1 hora"
            minutos > MINUTOS_MAXIMOS -> "El arriendo máximo es de 8 horas"
            else -> null
        }
    }

    private fun validarMotivo(valor: String): String? {
        val motivo = valor.trim()
        return when {
            motivo.length < 10 -> "Cuéntanos el motivo (mínimo 10 caracteres)"
            motivo.length > 200 -> "El motivo es demasiado largo (máximo 200)"
            else -> null
        }
    }

    private companion object {
        const val MESES_MAXIMOS = 6L
        const val MINUTOS_MINIMOS = 60L
        const val MINUTOS_MAXIMOS = 480L
        val HORA_APERTURA: LocalTime = LocalTime.of(8, 0)
        val HORA_CIERRE: LocalTime = LocalTime.of(22, 0)
    }
}

class ArriendoViewModelFactory(
    private val repository: ArriendoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ArriendoViewModel::class.java))
        return ArriendoViewModel(repository) as T
    }
}