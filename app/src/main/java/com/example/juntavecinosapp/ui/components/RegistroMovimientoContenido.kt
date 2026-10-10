package com.example.juntavecinosapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.juntavecinosapp.model.TipoMovimiento
import com.example.juntavecinosapp.ui.utils.Vibracion
import com.example.juntavecinosapp.ui.utils.formatearFecha
import com.example.juntavecinosapp.ui.utils.milisDeFechaSeleccionada
import com.example.juntavecinosapp.ui.utils.milisParaSelectorFecha
import com.example.juntavecinosapp.viewmodel.EventoFormulario
import com.example.juntavecinosapp.viewmodel.MovimientoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroMovimientoContenido(
    titulo: String,
    tipoInicial: String,
    etiquetaPersona: String,
    iconoPersona: ImageVector,
    mostrarSelectorTipo: Boolean,
    viewModel: MovimientoViewModel,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val form by viewModel.form.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var exito by remember { mutableStateOf(false) }
    var mostrarSelectorFecha by rememberSaveable { mutableStateOf(false) }

    // Se reinicia el formulario solo la primera vez (no al rotar la pantalla).
    var iniciado by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!iniciado) {
            viewModel.iniciarFormulario(tipoInicial)
            iniciado = true
        }
    }

    // Reacciona a los eventos del ViewModel: vibración, mensajes y volver.
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                EventoFormulario.Guardado -> {
                    Vibracion.exito(context)
                    exito = true
                    delay(1200)
                    onVolver()
                }
                EventoFormulario.Invalido -> Vibracion.error(context)
                is EventoFormulario.Error -> {
                    Vibracion.error(context)
                    launch { snackbarHostState.showSnackbar(evento.mensaje) }
                }
            }
        }
    }

    Scaffold(
        // El Scaffold de MainActivity ya aplica los márgenes del sistema.
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                windowInsets = WindowInsets(0)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            // Animación de entrada del formulario
            val entrada = remember { MutableTransitionState(false).apply { targetState = true } }
            AnimatedVisibility(
                visibleState = entrada,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 12 }
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)   // en tablets el formulario no se estira
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (mostrarSelectorTipo) {
                        val opciones = listOf(
                            TipoMovimiento.PAGO to "Pago de vecino",
                            TipoMovimiento.INGRESO to "Ingreso"
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            opciones.forEachIndexed { indice, (tipo, texto) ->
                                SegmentedButton(
                                    selected = form.tipo == tipo,
                                    onClick = { viewModel.onTipoChange(tipo) },
                                    shape = SegmentedButtonDefaults.itemShape(indice, opciones.size),
                                    label = { Text(texto) }
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    CampoValidado(
                        valor = form.concepto,
                        onValorChange = viewModel::onConceptoChange,
                        etiqueta = "Concepto",
                        error = form.errorConcepto,
                        iconoInicial = Icons.Filled.Description
                    )

                    CampoValidado(
                        valor = form.monto,
                        onValorChange = viewModel::onMontoChange,
                        etiqueta = "Monto",
                        error = form.errorMonto,
                        iconoInicial = Icons.Filled.AttachMoney,
                        tipoTeclado = KeyboardType.Number
                    )

                    // Campo de fecha: toda el área abre el selector
                    Box {
                        CampoValidado(
                            valor = formatearFecha(form.fecha),
                            onValorChange = {},
                            etiqueta = "Fecha",
                            error = form.errorFecha,
                            iconoInicial = Icons.Filled.CalendarMonth,
                            soloLectura = true
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { mostrarSelectorFecha = true }
                        )
                    }

                    CampoValidado(
                        valor = form.persona,
                        onValorChange = viewModel::onPersonaChange,
                        etiqueta = etiquetaPersona,
                        error = form.errorPersona,
                        iconoInicial = iconoPersona,
                        capitalizacion = KeyboardCapitalization.Words
                    )

                    CampoValidado(
                        valor = form.descripcion,
                        onValorChange = viewModel::onDescripcionChange,
                        etiqueta = "Descripción (opcional)",
                        error = form.errorDescripcion,
                        iconoInicial = Icons.Filled.Edit,
                        maxLineas = 3
                    )

                    ImagenInteligente(
                        uri = form.uriComprobante,
                        error = form.errorComprobante,
                        onUriChange = viewModel::onComprobanteChange,
                        onError = viewModel::onComprobanteError
                    )

                    Spacer(Modifier.height(8.dp))

                    // Botón con efecto de clic y estado de carga
                    val interaccion = remember { MutableInteractionSource() }
                    val presionado by interaccion.collectIsPressedAsState()
                    val escala by animateFloatAsState(
                        targetValue = if (presionado) 0.96f else 1f,
                        label = "escalaBoton"
                    )
                    Button(
                        onClick = viewModel::guardar,
                        enabled = !form.guardando,
                        interactionSource = interaccion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .graphicsLayer { scaleX = escala; scaleY = escala }
                    ) {
                        AnimatedContent(targetState = form.guardando, label = "contenidoBoton") { guardando ->
                            if (guardando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = LocalContentColor.current
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Save, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Guardar")
                                }
                            }
                        }
                    }
                }
            }

            // Confirmación animada al guardar
            AnimatedVisibility(
                visible = exito,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(96.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Movimiento guardado", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }

    if (mostrarSelectorFecha) {
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = milisParaSelectorFecha(form.fecha)
        )
        DatePickerDialog(
            onDismissRequest = { mostrarSelectorFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let {
                        viewModel.onFechaChange(milisDeFechaSeleccionada(it))
                    }
                    mostrarSelectorFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelectorFecha = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = estadoFecha) }
    }
}