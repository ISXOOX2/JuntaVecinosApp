package com.example.juntavecinosapp.data.model

import com.example.juntavecinosapp.model.Arriendo
import com.example.juntavecinosapp.model.EstadoArriendo
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Reserva de ms-reservas (en la app se llama Arriendo)
// fecha "yyyy-MM-dd", horas "HH:mm" (el servidor responde "HH:mm:ss")
data class ReservaDto(
    val id: Int? = null,
    val usuarioId: Int,
    val dependencia: String,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val motivo: String,
    val monto: Int? = null,
    val estado: String? = null
)

// Body de PATCH reservas/{id}/estado
data class EstadoRequest(
    val estado: String
)

private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

// Room -> API (el id y el monto los pone el servidor)
fun Arriendo.aReservaDto(): ReservaDto = ReservaDto(
    usuarioId = usuarioId,
    dependencia = dependencia,
    fecha = fecha.toString(),
    horaInicio = horaInicio.format(formatoHora),
    horaFin = horaFin.format(formatoHora),
    motivo = motivo
)

// API -> Room, con el id del servidor
fun ReservaDto.aArriendo(): Arriendo = Arriendo(
    id = id ?: 0,
    usuarioId = usuarioId,
    dependencia = dependencia,
    fecha = LocalDate.parse(fecha),
    horaInicio = LocalTime.parse(horaInicio),   // acepta "HH:mm" y "HH:mm:ss"
    horaFin = LocalTime.parse(horaFin),
    motivo = motivo,
    monto = monto ?: 0,
    estado = estado ?: EstadoArriendo.PENDIENTE
)
