package com.example.juntavecinosapp.data.repository

import com.example.juntavecinosapp.data.local.UsuarioDao
import com.example.juntavecinosapp.data.model.LoginRequest
import com.example.juntavecinosapp.data.model.RegistroRequest
import com.example.juntavecinosapp.data.model.aUsuario
import com.example.juntavecinosapp.data.model.mensajeDeError
import com.example.juntavecinosapp.data.remote.RetrofitInstance
import com.example.juntavecinosapp.model.Usuario
import retrofit2.HttpException
import java.io.IOException

/** Punto de acceso a los datos de usuarios. Los ViewModels usan esta clase, no el DAO. */
class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    /** Registra en ms-admin y guarda en Room con el id del servidor. Sin red: solo Room. */
    suspend fun registrar(usuario: Usuario): Long {
        return try {
            val remoto = RetrofitInstance.adminApi.registrar(
                RegistroRequest(usuario.nombre, usuario.correo, usuario.clave)
            )
            usuarioDao.insertar(remoto.aUsuario(usuario.clave))
        } catch (e: HttpException) {
            throw Exception(mensajeDeError(e))   // ej: correo ya registrado
        } catch (e: IOException) {
            usuarioDao.insertar(usuario)
        }
    }

    /** Login en ms-admin; si responde, se guarda en Room. Sin red: login local. */
    suspend fun iniciarSesion(correo: String, clave: String): Usuario? {
        return try {
            val remoto = RetrofitInstance.adminApi.login(LoginRequest(correo, clave))
            val usuario = remoto.aUsuario(clave)
            usuarioDao.insertar(usuario)
            usuario
        } catch (e: HttpException) {
            throw Exception(mensajeDeError(e))   // ej: credenciales incorrectas
        } catch (e: IOException) {
            usuarioDao.iniciarSesion(correo, clave)
        }
    }

    /** Sirve para saber si un correo ya está registrado. */
    suspend fun buscarPorCorreo(correo: String): Usuario? =
        usuarioDao.buscarPorCorreo(correo)

    suspend fun buscarPorId(id: Int): Usuario? = usuarioDao.buscarPorId(id)
}
