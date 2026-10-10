package com.example.juntavecinosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.juntavecinosapp.data.repository.MovimientoRepository
import com.example.juntavecinosapp.model.Movimiento
import com.example.juntavecinosapp.model.TipoMovimiento
import com.example.juntavecinosapp.ui.utils.formatearPesos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class PeriodoReporte(val etiqueta: String) {
    ESTE_MES("Este mes"),
    MES_ANTERIOR("Mes anterior"),
    HISTORICO("Todo")
}

/** Todo lo que la pantalla necesita mostrar, ya calculado. */
data class ReporteUiState(
    val periodo: PeriodoReporte = PeriodoReporte.ESTE_MES,
    val etiquetaPeriodo: String = "",
    val totalPagos: Long = 0L,
    val totalIngresos: Long = 0L,
    val entradas: Long = 0L,
    val salidas: Long = 0L,
    val balance: Long = 0L,
    val cantidad: Int = 0,
    val fraccionEntradas: Float = 0f,   // altura de la barra, de 0.0 a 1.0
    val fraccionSalidas: Float = 0f,
    val gastoMayor: Movimiento? = null
)

class ReporteViewModel(
    private val repository: MovimientoRepository
) : ViewModel() {

    private val periodoElegido = MutableStateFlow(PeriodoReporte.ESTE_MES)

    fun elegirPeriodo(periodo: PeriodoReporte) {
        periodoElegido.value = periodo
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val reporte: StateFlow<ReporteUiState> = periodoElegido
        .flatMapLatest { periodo ->
            val rango = rangoDe(periodo)
            val movimientos = if (rango == null) repository.movimientos
            else repository.movimientosPorRango(rango.first, rango.second)
            movimientos.map { lista -> construirReporte(periodo, lista) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReporteUiState())

    /** Texto plano del reporte, para compartir por WhatsApp, correo, etc. */
    fun generarResumen(): String {
        val r = reporte.value
        return buildString {
            appendLine("Reporte financiero - Junta de Vecinos")
            appendLine("Periodo: ${r.etiquetaPeriodo}")
            appendLine()
            appendLine("Pagos de vecinos: ${formatearPesos(r.totalPagos)}")
            appendLine("Otros ingresos: ${formatearPesos(r.totalIngresos)}")
            appendLine("Total entradas: ${formatearPesos(r.entradas)}")
            appendLine("Total gastos: ${formatearPesos(r.salidas)}")
            val saldo = if (r.balance < 0) "-" + formatearPesos(-r.balance) else formatearPesos(r.balance)
            appendLine("Saldo del periodo: $saldo")
            append("Movimientos registrados: ${r.cantidad}")
        }
    }

    // ---------- Cálculos ----------

    private fun construirReporte(periodo: PeriodoReporte, lista: List<Movimiento>): ReporteUiState {
        val pagos = lista.filter { it.tipo == TipoMovimiento.PAGO }.sumOf { it.monto }
        val ingresos = lista.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto }
        val gastos = lista.filter { it.tipo == TipoMovimiento.GASTO }
        val salidas = gastos.sumOf { it.monto }
        val entradas = pagos + ingresos
        val maximo = maxOf(entradas, salidas)

        return ReporteUiState(
            periodo = periodo,
            etiquetaPeriodo = etiquetaDe(periodo),
            totalPagos = pagos,
            totalIngresos = ingresos,
            entradas = entradas,
            salidas = salidas,
            balance = entradas - salidas,
            cantidad = lista.size,
            fraccionEntradas = if (maximo > 0) entradas.toFloat() / maximo else 0f,
            fraccionSalidas = if (maximo > 0) salidas.toFloat() / maximo else 0f,
            gastoMayor = gastos.maxByOrNull { it.monto }
        )
    }

    /** Rango [inicio, fin] en milisegundos. null = sin límite (todo el historial). */
    private fun rangoDe(periodo: PeriodoReporte): Pair<Long, Long>? {
        val zona = ZoneId.systemDefault()
        val primerDiaMes = LocalDate.now(zona).withDayOfMonth(1)

        fun rangoDelMes(primerDia: LocalDate): Pair<Long, Long> {
            val inicio = primerDia.atStartOfDay(zona).toInstant().toEpochMilli()
            val fin = primerDia.plusMonths(1).atStartOfDay(zona).toInstant().toEpochMilli() - 1
            return inicio to fin
        }

        return when (periodo) {
            PeriodoReporte.ESTE_MES -> rangoDelMes(primerDiaMes)
            PeriodoReporte.MES_ANTERIOR -> rangoDelMes(primerDiaMes.minusMonths(1))
            PeriodoReporte.HISTORICO -> null
        }
    }

    private fun etiquetaDe(periodo: PeriodoReporte): String {
        val primerDiaMes = LocalDate.now().withDayOfMonth(1)
        val mes = when (periodo) {
            PeriodoReporte.ESTE_MES -> primerDiaMes
            PeriodoReporte.MES_ANTERIOR -> primerDiaMes.minusMonths(1)
            PeriodoReporte.HISTORICO -> return "Todo el historial"
        }
        return DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-CL"))
            .format(mes)
            .replaceFirstChar { it.uppercase() }
    }
}

class ReporteViewModelFactory(
    private val repository: MovimientoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ReporteViewModel::class.java))
        return ReporteViewModel(repository) as T
    }
}