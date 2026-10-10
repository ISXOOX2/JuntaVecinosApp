package com.example.juntavecinosapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.juntavecinosapp.model.Usuario

/** Consultas de la tabla "usuarios". */
@Dao
interface UsuarioDao {

    /** Guarda un usuario nuevo y devuelve el id que le asignó Room. */
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    /** Devuelve el usuario si el correo y la clave coinciden, o null si no. */
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND clave = :clave LIMIT 1")
    suspend fun iniciarSesion(correo: String, clave: String): Usuario?

    /** Sirve para revisar que un correo no esté repetido al registrarse. */
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Usuario?
}