package com.example.juntavecinosapp.data.model

import com.example.juntavecinosapp.model.Movimiento

// Movimiento de ms-finanzas (fecha en milisegundos, igual que en Room)
data class MovimientoDto(
    val id: Int? = null,
    val tipo: String,
    val concepto: String,
    val monto: Long,
    val fecha: Long,
    val persona: String? = null,
    val descripcion: String? = null,
    val reservaId: Int? = null,
    val uriComprobante: String? = null
)

// Respuesta de GET movimientos/resumen
data class ResumenDto(
    val totalIngresos: Long,
    val totalGastos: Long,
    val saldo: Long
)

// Room -> API (sin id: lo asigna el servidor)
fun Movimiento.aMovimientoDto(): MovimientoDto = MovimientoDto(
    tipo = tipo,
    concepto = concepto,
    monto = monto,
    fecha = fecha,
    persona = persona,
    descripcion = descripcion,
    uriComprobante = uriComprobante
)

// API -> Room, con el id del servidor (reservaId no se guarda en Room)
fun MovimientoDto.aMovimiento(): Movimiento = Movimiento(
    id = id ?: 0,
    tipo = tipo,
    concepto = concepto,
    monto = monto,
    fecha = fecha,
    persona = persona ?: "",
    descripcion = descripcion ?: "",
    uriComprobante = uriComprobante
)
