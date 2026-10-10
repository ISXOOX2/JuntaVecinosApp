package com.example.juntavecinosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.juntavecinosapp.data.repository.UsuarioRepository
import com.example.juntavecinosapp.model.RolUsuario
import com.example.juntavecinosapp.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Formulario de inicio de sesión.
data class LoginFormState(
    val correo: String = "",
    val clave: String = "",
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorGeneral: String? = null,   // "correo o clave incorrectos"
    val cargando: Boolean = false
) {
    val tieneErrores: Boolean
        get() = errorCorreo != null || errorClave != null
}

/** Estado del formulario de registro de un vecino nuevo. */
data class RegistroFormState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmarClave: String = "",
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorConfirmar: String? = null,
    val cargando: Boolean = false
) {
    val tieneErrores: Boolean
        get() = listOf(errorNombre, errorCorreo, errorClave, errorConfirmar).any { it != null }
}

//Lógica de acceso: valida los campos, consulta el repositorio y guarda quién inició sesión.
//Las pantallas solo muestran el estado y avisan lo que escribe el usuario.

class AuthViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    private val _login = MutableStateFlow(LoginFormState())
    val login: StateFlow<LoginFormState> = _login.asStateFlow()

    private val _registro = MutableStateFlow(RegistroFormState())
    val registro: StateFlow<RegistroFormState> = _registro.asStateFlow()

    /** Usuario que inició sesión (null si no hay sesión). */
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    /** Guardado = login o registro exitoso; la pantalla navega al Home. */
    private val _eventos = Channel<EventoFormulario>(Channel.BUFFERED)
    val eventos: Flow<EventoFormulario> = _eventos.receiveAsFlow()

    // ======================= LOGIN =======================

    fun onLoginCorreoChange(valor: String) = _login.update {
        it.copy(correo = valor, errorCorreo = validarCorreo(valor), errorGeneral = null)
    }

    fun onLoginClaveChange(valor: String) = _login.update {
        it.copy(clave = valor, errorClave = validarClaveLogin(valor), errorGeneral = null)
    }

    fun iniciarSesion() {
        val actual = _login.value
        if (actual.cargando) return   // evita doble clic

        val validado = actual.copy(
            errorCorreo = validarCorreo(actual.correo),
            errorClave = validarClaveLogin(actual.clave)
        )
        _login.value = validado
        if (validado.tieneErrores) {
            _eventos.trySend(EventoFormulario.Invalido)
            return
        }

        _login.update { it.copy(cargando = true) }
        viewModelScope.launch {
            try {
                val usuario = repository.iniciarSesion(
                    correo = actual.correo.trim().lowercase(),
                    clave = actual.clave
                )
                if (usuario == null) {
                    _login.update {
                        it.copy(cargando = false, errorGeneral = "Correo o clave incorrectos")
                    }
                    _eventos.send(EventoFormulario.Invalido)
                } else {
                    _usuarioActual.value = usuario
                    _login.value = LoginFormState()
                    _eventos.send(EventoFormulario.Guardado)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _login.update { it.copy(cargando = false) }
                _eventos.send(EventoFormulario.Error("No se pudo iniciar sesión. Intenta de nuevo."))
            }
        }
    }

    // ======================= REGISTRO =======================

    fun onRegistroNombreChange(valor: String) = _registro.update {
        it.copy(nombre = valor, errorNombre = validarNombre(valor))
    }

    fun onRegistroCorreoChange(valor: String) = _registro.update {
        it.copy(correo = valor, errorCorreo = validarCorreo(valor))
    }

    fun onRegistroClaveChange(valor: String) = _registro.update {
        it.copy(
            clave = valor,
            errorClave = validarClaveRegistro(valor),
            // si ya escribió la confirmación, se vuelve a comparar
            errorConfirmar = if (it.confirmarClave.isNotEmpty())
                validarConfirmacion(valor, it.confirmarClave) else it.errorConfirmar
        )
    }

    fun onRegistroConfirmarChange(valor: String) = _registro.update {
        it.copy(confirmarClave = valor, errorConfirmar = validarConfirmacion(it.clave, valor))
    }

    fun registrar() {
        val actual = _registro.value
        if (actual.cargando) return

        val validado = actual.copy(
            errorNombre = validarNombre(actual.nombre),
            errorCorreo = validarCorreo(actual.correo),
            errorClave = validarClaveRegistro(actual.clave),
            errorConfirmar = validarConfirmacion(actual.clave, actual.confirmarClave)
        )
        _registro.value = validado
        if (validado.tieneErrores) {
            _eventos.trySend(EventoFormulario.Invalido)
            return
        }

        _registro.update { it.copy(cargando = true) }
        viewModelScope.launch {
            try {
                val correo = actual.correo.trim().lowercase()

                // regla de negocio: un correo no se puede registrar dos veces
                if (repository.buscarPorCorreo(correo) != null) {
                    _registro.update {
                        it.copy(cargando = false, errorCorreo = "Este correo ya está registrado")
                    }
                    _eventos.send(EventoFormulario.Invalido)
                    return@launch
                }

                val nuevo = Usuario(
                    nombre = actual.nombre.trim(),
                    correo = correo,
                    clave = actual.clave,
                    rol = RolUsuario.VECINO   // quien se registra siempre es vecino
                )
                val id = repository.registrar(nuevo)

                _usuarioActual.value = nuevo.copy(id = id.toInt())
                _registro.value = RegistroFormState()
                _eventos.send(EventoFormulario.Guardado)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _registro.update { it.copy(cargando = false) }
                _eventos.send(EventoFormulario.Error("No se pudo crear la cuenta. Intenta de nuevo."))
            }
        }
    }

    // ======================= SESIÓN Y LIMPIEZA =======================

    fun cerrarSesion() {
        _usuarioActual.value = null
    }

    /** Se llama al abrir la pantalla de acceso para no arrastrar textos o errores viejos. */
    fun limpiarFormularios() {
        _login.value = LoginFormState()
        _registro.value = RegistroFormState()
    }

    // ======================= VALIDACIONES =======================
    // Funciones puras (no tocan la pantalla ni la base): devuelven el mensaje de error o null.

    private fun validarNombre(valor: String): String? {
        val nombre = valor.trim()
        return when {
            nombre.isEmpty() -> "Ingresa tu nombre"
            nombre.length < 3 -> "El nombre debe tener al menos 3 letras"
            nombre.length > 50 -> "El nombre es demasiado largo"
            !nombre.all { it.isLetter() || it == ' ' || it == '\'' } ->
                "Usa solo letras y espacios"
            else -> null
        }
    }

    private fun validarCorreo(valor: String): String? {
        val correo = valor.trim()
        return when {
            correo.isEmpty() -> "Ingresa tu correo"
            !REGEX_CORREO.matches(correo) -> "Escribe un correo válido (ej: nombre@correo.cl)"
            else -> null
        }
    }

    /** En el login solo se exige que no esté vacía; las reglas de formato son del registro. */
    private fun validarClaveLogin(valor: String): String? =
        if (valor.isEmpty()) "Ingresa tu clave" else null

    private fun validarClaveRegistro(valor: String): String? = when {
        valor.isEmpty() -> "Crea una clave"
        valor.length < 6 -> "La clave debe tener al menos 6 caracteres"
        valor.length > 30 -> "La clave es demasiado larga"
        !valor.any { it.isLetter() } || !valor.any { it.isDigit() } ->
            "La clave debe combinar letras y números"
        else -> null
    }

    private fun validarConfirmacion(clave: String, confirmar: String): String? = when {
        confirmar.isEmpty() -> "Repite la clave"
        clave != confirmar -> "Las claves no coinciden"
        else -> null
    }

    private companion object {
        val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}

class AuthViewModelFactory(
    private val repository: UsuarioRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AuthViewModel::class.java))
        return AuthViewModel(repository) as T
    }
}