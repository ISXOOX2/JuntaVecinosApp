package com.example.juntavecinosapp.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.juntavecinosapp.ui.utils.formatearFecha
import com.example.juntavecinosapp.ui.utils.formatearPesos
import com.example.juntavecinosapp.viewmodel.PeriodoReporte
import com.example.juntavecinosapp.viewmodel.ReporteViewModel

private val VerdeEntrada = Color(0xFF2E7D32)
private val AlturaMaximaBarra = 140.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteScreen(
    viewModel: ReporteViewModel,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val reporte by viewModel.reporte.collectAsStateWithLifecycle()
    val periodos = PeriodoReporte.entries

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Reporte") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                windowInsets = WindowInsets(0)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            val entrada = remember { MutableTransitionState(false).apply { targetState = true } }
            AnimatedVisibility(
                visibleState = entrada,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 12 }
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 700.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ---------- Selector de periodo ----------
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        periodos.forEachIndexed { indice, periodo ->
                            SegmentedButton(
                                selected = reporte.periodo == periodo,
                                onClick = { viewModel.elegirPeriodo(periodo) },
                                shape = SegmentedButtonDefaults.itemShape(indice, periodos.size),
                                label = { Text(periodo.etiqueta) }
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = reporte.etiquetaPeriodo,
                        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                        label = "tituloPeriodo"
                    ) { titulo ->
                        Text(titulo, style = MaterialTheme.typography.titleLarge)
                    }

                    if (reporte.cantidad == 0) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No hay movimientos en este periodo",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    } else {
                        // ---------- Gráfico de barras ----------
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Entradas y salidas", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Barra("Entradas", reporte.entradas, reporte.fraccionEntradas, VerdeEntrada)
                                    Barra("Salidas", reporte.salidas, reporte.fraccionSalidas, MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        // ---------- Resumen ----------
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Resumen del periodo", style = MaterialTheme.typography.titleMedium)
                                FilaDato("Pagos de vecinos", formatearPesos(reporte.totalPagos), VerdeEntrada)
                                FilaDato("Otros ingresos", formatearPesos(reporte.totalIngresos), VerdeEntrada)
                                FilaDato("Gastos", formatearPesos(reporte.salidas), MaterialTheme.colorScheme.error)
                                HorizontalDivider()
                                FilaDato(
                                    etiqueta = "Saldo del periodo",
                                    valor = if (reporte.balance < 0) "-" + formatearPesos(-reporte.balance)
                                    else formatearPesos(reporte.balance),
                                    color = if (reporte.balance < 0) MaterialTheme.colorScheme.error else VerdeEntrada,
                                    destacado = true
                                )
                                FilaDato(
                                    "Movimientos registrados",
                                    reporte.cantidad.toString(),
                                    MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // ---------- Gasto más alto ----------
                        reporte.gastoMayor?.let { gasto ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Gasto más alto", style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(8.dp))
                                    Text(gasto.concepto, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = formatearFecha(gasto.fecha) +
                                                if (gasto.persona.isNotBlank()) " · ${gasto.persona}" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatearPesos(gasto.monto),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        // ---------- Compartir ----------
                        Button(
                            onClick = {
                                val envio = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Reporte financiero - Junta de Vecinos")
                                    putExtra(Intent.EXTRA_TEXT, viewModel.generarResumen())
                                }
                                context.startActivity(Intent.createChooser(envio, "Compartir reporte"))
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Compartir resumen")
                        }
                    }
                }
            }
        }
    }
}

/** Barra vertical que crece con animación hasta su altura. */
@Composable
private fun Barra(etiqueta: String, monto: Long, fraccion: Float, color: Color) {
    val fraccionAnimada by animateFloatAsState(
        targetValue = fraccion,
        animationSpec = tween(800),
        label = "alturaBarra"
    )
    Column(
        modifier = Modifier.height(AlturaMaximaBarra + 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            formatearPesos(monto),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(AlturaMaximaBarra * fraccionAnimada)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color)
        )
        Spacer(Modifier.height(6.dp))
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FilaDato(
    etiqueta: String,
    valor: String,
    color: Color,
    destacado: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            etiqueta,
            style = if (destacado) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
        )
        Text(
            valor,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}