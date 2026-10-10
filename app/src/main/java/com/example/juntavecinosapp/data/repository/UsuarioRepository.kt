package com.example.juntavecinosapp.data.repository

import com.example.juntavecinosapp.data.local.UsuarioDao
import com.example.juntavecinosapp.model.Usuario

/** Punto de acceso a los datos de usuarios. Los ViewModels usan esta clase, no el DAO. */
class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    /** Guarda un usuario nuevo y devuelve su id. */
    suspend fun registrar(usuario: Usuario): Long = usuarioDao.insertar(usuario)

    /** Devuelve el usuario si correo y clave coinciden, o null. */
    suspend fun iniciarSesion(correo: String, clave: String): Usuario? =
        usuarioDao.iniciarSesion(correo, clave)

    /** Sirve para saber si un correo ya está registrado. */
    suspend fun buscarPorCorreo(correo: String): Usuario? =
        usuarioDao.buscarPorCorreo(correo)

    suspend fun buscarPorId(id: Int): Usuario? = usuarioDao.buscarPorId(id)
}