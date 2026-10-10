package com.example.juntavecinosapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.juntavecinosapp.model.Dependencias
import com.example.juntavecinosapp.ui.components.CampoValidado
import com.example.juntavecinosapp.ui.utils.Vibracion
import com.example.juntavecinosapp.ui.utils.formatearPesos
import com.example.juntavecinosapp.viewmodel.ArriendoViewModel
import com.example.juntavecinosapp.viewmodel.EventoFormulario
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarArriendoScreen(
    viewModel: ArriendoViewModel,
    usuarioId: Int,
    onVolver: () -> Unit
) {
    val form by viewModel.form.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var mostrarFecha by rememberSaveable { mutableStateOf(false) }
    var mostrarHoraInicio by rememberSaveable { mutableStateOf(false) }
    var mostrarHoraFin by rememberSaveable { mutableStateOf(false) }
    var mostrarConfirmacion by rememberSaveable { mutableStateOf(false) }

    // Al entrar siempre partimos con el formulario limpio.
    LaunchedEffect(Unit) { viewModel.iniciarFormulario() }

    // Eventos que manda el ViewModel: guardado, inválido o error.
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                EventoFormulario.Guardado -> {
                    Vibracion.exito(context)
                    mostrarConfirmacion = true
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
                title = { Text("Solicitar arriendo") },
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
        // Box + widthIn: en tablet el formulario queda centrado y no se estira.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Elige la dependencia y el horario. La directiva revisará tu solicitud.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                SelectorDependencia(
                    seleccionada = form.dependencia,
                    error = form.errorDependencia,
                    onSeleccion = viewModel::onDependenciaChange
                )

                CampoSelector(
                    texto = form.fecha?.format(formatoFecha) ?: "",
                    etiqueta = "Fecha",
                    icono = Icons.Filled.CalendarMonth,
                    error = form.errorFecha,
                    onClick = { mostrarFecha = true }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        CampoSelector(
                            texto = form.horaInicio?.format(formatoHora) ?: "",
                            etiqueta = "Desde",
                            icono = Icons.Filled.Schedule,
                            error = form.errorHoraInicio,
                            onClick = { mostrarHoraInicio = true }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        CampoSelector(
                            texto = form.horaFin?.format(formatoHora) ?: "",
                            etiqueta = "Hasta",
                            icono = Icons.Filled.AccessTime,
                            error = form.errorHoraFin,
                            onClick = { mostrarHoraFin = true }
                        )
                    }
                }

                CampoValidado(
                    valor = form.motivo,
                    onValorChange = viewModel::onMotivoChange,
                    etiqueta = "Motivo del arriendo",
                    error = form.errorMotivo,
                    iconoInicial = Icons.Filled.Notes,
                    capitalizacion = KeyboardCapitalization.Sentences,
                    maxLineas = 3
                )

                // El monto aparece (con animación) apenas hay dependencia y horas válidas.
                AnimatedVisibility(
                    visible = form.montoEstimado != null,
                    enter = fadeIn() + expandVertically()
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Monto estimado",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatearPesos((form.montoEstimado ?: 0).toLong()),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Button(
                    onClick = { viewModel.solicitar(usuarioId) },
                    enabled = !form.cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    if (form.cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 2.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Enviar solicitud")
                    }
                }
            }
        }
    }

    // ---------- Diálogos ----------

    if (mostrarFecha) {
        // El selector de Material trabaja en UTC, por eso se convierte con ZoneOffset.UTC.
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = (form.fecha ?: LocalDate.now())
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { mostrarFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let { millis ->
                        val fecha = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.onFechaChange(fecha)
                    }
                    mostrarFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarFecha = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = estadoFecha) }
    }

    if (mostrarHoraInicio) {
        DialogoHora(
            titulo = "Hora de inicio",
            inicial = form.horaInicio ?: LocalTime.of(10, 0),
            onConfirmar = {
                viewModel.onHoraInicioChange(it)
                mostrarHoraInicio = false
            },
            onCancelar = { mostrarHoraInicio = false }
        )
    }

    if (mostrarHoraFin) {
        DialogoHora(
            titulo = "Hora de término",
            inicial = form.horaFin ?: (form.horaInicio?.plusHours(2) ?: LocalTime.of(12, 0)),
            onConfirmar = {
                viewModel.onHoraFinChange(it)
                mostrarHoraFin = false
            },
            onCancelar = { mostrarHoraFin = false }
        )
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Solicitud enviada") },
            text = { Text("Tu solicitud quedó pendiente. La directiva la aprobará o rechazará.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    onVolver()
                }) { Text("Volver al inicio") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    viewModel.iniciarFormulario()
                }) { Text("Solicitar otro") }
            }
        )
    }
}

// Campo de solo lectura que abre un diálogo al tocarlo (fecha y horas).
@Composable
private fun CampoSelector(
    texto: String,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    onClick: () -> Unit
) {
    Box {
        CampoValidado(
            valor = texto,
            onValorChange = {},
            etiqueta = etiqueta,
            error = error,
            iconoInicial = icono,
            soloLectura = true
        )
        // Capa transparente que recibe el toque (el campo readOnly no abre nada solo).
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = 22.dp)
                .clickable(onClick = onClick)
        )
    }
}

// Lista desplegable con las dependencias y su tarifa por hora.
@Composable
private fun SelectorDependencia(
    seleccionada: String,
    error: String?,
    onSeleccion: (String) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        CampoSelector(
            texto = seleccionada,
            etiqueta = "Dependencia",
            icono = Icons.Filled.Home,
            error = error,
            onClick = { abierto = true }
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            Dependencias.lista.forEach { dependencia ->
                DropdownMenuItem(
                    text = {
                        Text("${dependencia.nombre} · ${formatearPesos(dependencia.tarifaPorHora.toLong())}/hora")
                    },
                    onClick = {
                        onSeleccion(dependencia.nombre)
                        abierto = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoHora(
    titulo: String,
    inicial: LocalTime,
    onConfirmar: (LocalTime) -> Unit,
    onCancelar: () -> Unit
) {
    val estado = rememberTimePickerState(
        initialHour = inicial.hour,
        initialMinute = inicial.minute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { TimePicker(state = estado) },
        confirmButton = {
            TextButton(onClick = { onConfirmar(LocalTime.of(estado.hour, estado.minute)) }) {
                Text("Aceptar")
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}