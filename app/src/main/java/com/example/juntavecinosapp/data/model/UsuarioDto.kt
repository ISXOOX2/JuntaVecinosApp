package com.example.juntavecinosapp.data.model

import com.example.juntavecinosapp.model.RolUsuario
import com.example.juntavecinosapp.model.Usuario

// Usuario que devuelve ms-admin (sin clave)
data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String?,
    val activo: Boolean?
)

// Body de POST auth/login
data class LoginRequest(
    val email: String,
    val password: String
)

// Body de POST auth/register
data class RegistroRequest(
    val nombre: String,
    val email: String,
    val password: String
)

// La API usa "email" y la app "correo"; la clave se guarda solo en Room
fun UsuarioDto.aUsuario(clave: String): Usuario = Usuario(
    id = id,
    nombre = nombre,
    correo = email,
    clave = clave,
    rol = rol ?: RolUsuario.VECINO
)
