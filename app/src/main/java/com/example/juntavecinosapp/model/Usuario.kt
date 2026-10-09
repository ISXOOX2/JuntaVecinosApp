package com.example.juntavecinosapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Roles posibles de un usuario, según el caso (vecino, directiva/secretaría, tesorería). */
object RolUsuario {
    const val VECINO = "VECINO"
    const val DIRECTIVA = "DIRECTIVA"
    const val TESORERIA = "TESORERIA"
}

//Tabla "usuarios" - Todos los datos son ficticios, como exige el caso.
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: String = RolUsuario.VECINO
)