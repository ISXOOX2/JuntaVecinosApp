package com.example.juntavecinosapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.juntavecinosapp.model.EstadoArriendo
import com.example.juntavecinosapp.model.RolUsuario
import com.example.juntavecinosapp.ui.components.EstadoChip
import com.example.juntavecinosapp.ui.utils.Vibracion
import com.example.juntavecinosapp.ui.utils.formatearPesos
import com.example.juntavecinosapp.viewmodel.ArriendoViewModel
import com.example.juntavecinosapp.viewmodel.EventoFormulario
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val formatoFechaLarga: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.forLanguageTag("es-CL"))
private val formatoHora: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleArriendoScreen(
    viewModel: ArriendoViewModel,
    arriendoId: Int,
    rol: String,
    onVolver: () -> Unit
) {
    val detalle by viewModel.detalle.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado que se quiere aplicar; mientras no sea null se muestra el diálogo de confirmación.
    var estadoPorConfirmar by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(arriendoId) { viewModel.cargarDetalle(arriendoId) }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                EventoFormulario.Guardado -> {
                    Vibracion.exito(context)
                    snackbarHostState.showSnackbar("Solicitud actualizada")
                }
                EventoFormulario.Invalido -> Vibracion.error(context)
                is EventoFormulario.Error -> {
                    Vibracion.error(context)
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del arriendo") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            // Puede quedar guardado el detalle anterior mientras carga el nuevo.
            val arriendo = detalle?.takeIf { it.id == arriendoId }

            if (arriendo == null) {
                CircularProgressIndicator(modifier = Modifier.padding(48.dp))
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = arriendo.dependencia,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        EstadoChip(estado = arriendo.estado)
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilaDato(Icons.Filled.Home, "Dependencia", arriendo.dependencia)
                            HorizontalDivider()
                            FilaDato(
                                Icons.Filled.CalendarMonth,
                                "Fecha",
                                arriendo.fecha.format(formatoFechaLarga).replaceFirstChar { it.uppercase() }
                            )
                            HorizontalDivider()
                            FilaDato(
                                Icons.Filled.AccessTime,
                                "Horario",
                                "${arriendo.horaInicio.format(formatoHora)} a ${arriendo.horaFin.format(formatoHora)}"
                            )
                            HorizontalDivider()
                            FilaDato(Icons.AutoMirrored.Filled.Notes, "Motivo", arriendo.motivo)
                            HorizontalDivider()
                            FilaDato(Icons.Filled.Paid, "Monto", formatearPesos(arriendo.monto.toLong()))
                        }
                    }

                    // Solo la directiva, y solo mientras la solicitud sigue pendiente.
                    AnimatedVisibility(
                        visible = rol == RolUsuario.DIRECTIVA && arriendo.estado == EstadoArriendo.PENDIENTE,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { estadoPorConfirmar = EstadoArriendo.RECHAZADO },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text("Rechazar")
                            }
                            Button(
                                onClick = { estadoPorConfirmar = EstadoArriendo.APROBADO },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text("Aprobar")
                            }
                        }
                    }
                }
            }
        }
    }

    estadoPorConfirmar?.let { nuevoEstado ->
        val aprobando = nuevoEstado == EstadoArriendo.APROBADO
        AlertDialog(
            onDismissRequest = { estadoPorConfirmar = null },
            title = { Text(if (aprobando) "¿Aprobar solicitud?" else "¿Rechazar solicitud?") },
            text = {
                Text(
                    if (aprobando) "El horario quedará reservado para el vecino."
                    else "El horario volverá a estar disponible."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resolverSolicitud(arriendoId, nuevoEstado, rol)
                    estadoPorConfirmar = null
                }) { Text(if (aprobando) "Aprobar" else "Rechazar") }
            },
            dismissButton = {
                TextButton(onClick = { estadoPorConfirmar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FilaDato(icono: ImageVector, etiqueta: String, valor: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.size(16.dp))
        Column {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = valor, style = MaterialTheme.typography.bodyLarge)
        }
    }
}