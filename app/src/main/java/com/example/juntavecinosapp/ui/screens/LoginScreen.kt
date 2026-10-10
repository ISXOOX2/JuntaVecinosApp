package com.example.juntavecinosapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.juntavecinosapp.ui.components.CampoClave
import com.example.juntavecinosapp.ui.components.CampoValidado
import com.example.juntavecinosapp.ui.utils.TipoPantalla
import com.example.juntavecinosapp.ui.utils.Vibracion
import com.example.juntavecinosapp.ui.utils.tipoPantallaActual
import com.example.juntavecinosapp.viewmodel.AuthViewModel
import com.example.juntavecinosapp.viewmodel.EventoFormulario
import com.example.juntavecinosapp.viewmodel.LoginFormState
import com.example.juntavecinosapp.viewmodel.RegistroFormState

/**
 * Pantalla de acceso: iniciar sesión o crear cuenta.
 * No tiene lógica: muestra el estado del AuthViewModel y le avisa lo que escribe el usuario.
 * En pantallas grandes se divide en dos paneles (diseño adaptable).
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onAccesoExitoso: () -> Unit
) {
    val context = LocalContext.current
    val login by viewModel.login.collectAsState()
    val registro by viewModel.registro.collectAsState()
    var modoRegistro by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val accesoExitoso by rememberUpdatedState(onAccesoExitoso)

    // Reacciona a los eventos del ViewModel: vibra y navega o muestra el aviso
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                EventoFormulario.Guardado -> {
                    Vibracion.exito(context)
                    accesoExitoso()
                }
                EventoFormulario.Invalido -> Vibracion.error(context)
                is EventoFormulario.Error -> {
                    Vibracion.error(context)
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    val tipoPantalla = tipoPantallaActual()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        if (tipoPantalla == TipoPantalla.EXPANDIDA) {
            // Pantalla grande: logo a la izquierda, formulario a la derecha
            Row(modifier = Modifier.fillMaxSize().padding(padding)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    EncabezadoApp(colorTexto = MaterialTheme.colorScheme.onPrimary)
                }
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    PanelAcceso(
                        modoRegistro = modoRegistro,
                        onCambiarModo = { modoRegistro = it },
                        login = login,
                        registro = registro,
                        viewModel = viewModel,
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .verticalScroll(rememberScrollState())
                            .imePadding()
                            .padding(32.dp)
                    )
                }
            }
        } else {
            // Celular o tablet chica: todo en una columna
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EncabezadoApp(colorTexto = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                PanelAcceso(
                    modoRegistro = modoRegistro,
                    onCambiarModo = { modoRegistro = it },
                    login = login,
                    registro = registro,
                    viewModel = viewModel,
                    modifier = Modifier.widthIn(max = 480.dp)
                )
            }
        }
    }
}

/** Logo y nombre de la organización. */
@Composable
private fun EncabezadoApp(colorTexto: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = null,
            tint = colorTexto,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "Junta de Vecinos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colorTexto,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Población Zaror",
            style = MaterialTheme.typography.titleMedium,
            color = colorTexto
        )
        Text(
            text = "Arriendos y transparencia financiera",
            style = MaterialTheme.typography.bodyMedium,
            color = colorTexto.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
    }
}

/** Selector "Iniciar sesión / Crear cuenta" y el formulario que corresponde. */
@Composable
private fun PanelAcceso(
    modoRegistro: Boolean,
    onCambiarModo: (Boolean) -> Unit,
    login: LoginFormState,
    registro: RegistroFormState,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !modoRegistro,
                onClick = { onCambiarModo(false) },
                label = { Text("Iniciar sesión") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = modoRegistro,
                onClick = { onCambiarModo(true) },
                label = { Text("Crear cuenta") },
                modifier = Modifier.weight(1f)
            )
        }

        // Animación al cambiar entre los dos formularios
        AnimatedContent(
            targetState = modoRegistro,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
            label = "modoAcceso"
        ) { esRegistro ->
            if (esRegistro) {
                FormularioRegistro(registro, viewModel)
            } else {
                FormularioLogin(login, viewModel)
            }
        }
    }
}

@Composable
private fun FormularioLogin(estado: LoginFormState, viewModel: AuthViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CampoValidado(
            valor = estado.correo,
            onValorChange = viewModel::onLoginCorreoChange,
            etiqueta = "Correo",
            error = estado.errorCorreo,
            iconoInicial = Icons.Filled.Email,
            tipoTeclado = KeyboardType.Email,
            capitalizacion = KeyboardCapitalization.None
        )
        CampoClave(
            valor = estado.clave,
            onValorChange = viewModel::onLoginClaveChange,
            etiqueta = "Clave",
            error = estado.errorClave
        )

        // Aviso de "correo o clave incorrectos"
        AnimatedVisibility(visible = estado.errorGeneral != null) {
            Text(
                text = estado.errorGeneral.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(8.dp))
        BotonAcceso(
            texto = "Entrar",
            cargando = estado.cargando,
            onClick = viewModel::iniciarSesion
        )
    }
}

@Composable
private fun FormularioRegistro(estado: RegistroFormState, viewModel: AuthViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CampoValidado(
            valor = estado.nombre,
            onValorChange = viewModel::onRegistroNombreChange,
            etiqueta = "Nombre completo",
            error = estado.errorNombre,
            iconoInicial = Icons.Filled.Person,
            capitalizacion = KeyboardCapitalization.Words
        )
        CampoValidado(
            valor = estado.correo,
            onValorChange = viewModel::onRegistroCorreoChange,
            etiqueta = "Correo",
            error = estado.errorCorreo,
            iconoInicial = Icons.Filled.Email,
            tipoTeclado = KeyboardType.Email,
            capitalizacion = KeyboardCapitalization.None
        )
        CampoClave(
            valor = estado.clave,
            onValorChange = viewModel::onRegistroClaveChange,
            etiqueta = "Clave",
            error = estado.errorClave
        )
        CampoClave(
            valor = estado.confirmarClave,
            onValorChange = viewModel::onRegistroConfirmarChange,
            etiqueta = "Repite la clave",
            error = estado.errorConfirmar
        )

        Spacer(Modifier.height(8.dp))
        BotonAcceso(
            texto = "Crear cuenta",
            cargando = estado.cargando,
            onClick = viewModel::registrar
        )
    }
}

/** Botón principal; mientras trabaja muestra una ruedita y no deja volver a presionarlo. */
@Composable
private fun BotonAcceso(texto: String, cargando: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current
            )
        } else {
            Text(texto)
        }
    }
}