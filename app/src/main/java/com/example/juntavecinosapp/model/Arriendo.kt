package com.example.juntavecinosapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime


object EstadoArriendo {
    const val PENDIENTE = "PENDIENTE"
    const val APROBADO = "APROBADO"
    const val RECHAZADO = "RECHAZADO"
}

//Tabla "arriendos"- solicitud de uso de una dependencia de la sede.
@Entity(tableName = "arriendos")
data class Arriendo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val dependencia: String,
    val fecha: LocalDate,
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    val motivo: String,
    val monto: Int,           // en pesos, ficticio
    val estado: String = EstadoArriendo.PENDIENTE

)