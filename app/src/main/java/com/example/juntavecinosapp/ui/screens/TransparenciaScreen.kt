package com.example.juntavecinosapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.juntavecinosapp.model.Movimiento
import com.example.juntavecinosapp.ui.utils.formatearFecha
import com.example.juntavecinosapp.ui.utils.formatearPesos
import com.example.juntavecinosapp.viewmodel.MovimientoViewModel

private val VerdeEntrada = Color(0xFF2E7D32)

private fun pesosConSigno(monto: Long): String =
    if (monto < 0) "-" + formatearPesos(-monto) else formatearPesos(monto)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransparenciaScreen(
    viewModel: MovimientoViewModel,
    onVolver: () -> Unit
) {
    val entradas by viewModel.totalEntradas.collectAsStateWithLifecycle()
    val salidas by viewModel.totalSalidas.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val pagos by viewModel.totalPagos.collectAsStateWithLifecycle()
    val ingresos by viewModel.totalIngresos.collectAsStateWithLifecycle()
    val porcentaje by viewModel.porcentajeGastado.collectAsStateWithLifecycle()
    val ultimosGastos by viewModel.ultimosGastos.collectAsStateWithLifecycle()

    val porcentajeAnimado by animateFloatAsState(
        targetValue = porcentaje,
        animationSpec = tween(800),
        label = "porcentajeGastado"
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Transparencia") },
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
                    // ---------- Saldo ----------
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (balance >= 0) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Saldo actual", style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(Modifier.height(8.dp))
                            MontoAnimado(
                                monto = balance,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // ---------- Entradas y salidas ----------
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TarjetaResumen(
                            titulo = "Entradas",
                            monto = entradas,
                            color = VerdeEntrada,
                            icono = {
                                Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = VerdeEntrada)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        TarjetaResumen(
                            titulo = "Salidas",
                            monto = salidas,
                            color = MaterialTheme.colorScheme.error,
                            icono = {
                                Icon(
                                    Icons.Filled.ArrowDownward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ---------- Uso de los fondos ----------
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Uso de los fondos", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { porcentajeAnimado },
                                modifier = Modifier.fillMaxWidth().height(10.dp),
                                color = if (porcentaje > 0.8f) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Se ha gastado el ${(porcentajeAnimado * 100).toInt()}% de lo recaudado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // ---------- Desglose ----------
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Desglose", style = MaterialTheme.typography.titleMedium)
                            FilaDesglose("Pagos de vecinos", pagos, VerdeEntrada)
                            HorizontalDivider()
                            FilaDesglose("Otros ingresos", ingresos, VerdeEntrada)
                            HorizontalDivider()
                            FilaDesglose("Gastos de la junta", salidas, MaterialTheme.colorScheme.error)
                        }
                    }

                    // ---------- Últimos gastos ----------
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Últimos gastos", style = MaterialTheme.typography.titleMedium)
                            if (ultimosGastos.isEmpty()) {
                                Text(
                                    "Aún no hay gastos registrados",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                ultimosGastos.forEachIndexed { indice, gasto ->
                                    if (indice > 0) HorizontalDivider()
                                    FilaGasto(gasto)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Cuenta desde 0 hasta el monto. Se calcula sobre el Long para no perder pesos al redondear. */
@Composable
private fun MontoAnimado(monto: Long, style: TextStyle, color: Color) {
    val avance = remember { Animatable(0f) }
    LaunchedEffect(monto) {
        avance.snapTo(0f)
        avance.animateTo(1f, tween(900))
    }
    Text(
        text = pesosConSigno((monto.toDouble() * avance.value).toLong()),
        style = style,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
private fun TarjetaResumen(
    titulo: String,
    monto: Long,
    color: Color,
    icono: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icono()
                Spacer(Modifier.width(6.dp))
                Text(titulo, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(8.dp))
            MontoAnimado(monto = monto, style = MaterialTheme.typography.titleLarge, color = color)
        }
    }
}

@Composable
private fun FilaDesglose(etiqueta: String, monto: Long, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text(
            formatearPesos(monto),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun FilaGasto(gasto: Movimiento) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                gasto.concepto,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(formatearFecha(gasto.fecha))
                    if (gasto.persona.isNotBlank()) append(" · ").append(gasto.persona)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (gasto.uriComprobante != null) {
            Icon(
                Icons.Filled.AttachFile,
                contentDescription = "Con comprobante",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            "- " + formatearPesos(gasto.monto),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error
        )
    }
}