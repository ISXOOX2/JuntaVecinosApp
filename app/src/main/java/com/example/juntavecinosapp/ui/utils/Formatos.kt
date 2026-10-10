package com.example.juntavecinosapp.ui.utils

import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** 1696939200000 -> "10/10/2023" (en la zona horaria del equipo). */
fun formatearFecha(millis: Long): String =
    formatoFecha.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

/** 15000 -> "$15.000" */
fun formatearPesos(monto: Long): String =
    "$" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CL")).format(monto)

/**
 * El selector de fechas de Material trabaja en UTC. Estas dos funciones
 * convierten entre UTC y la hora local para que no se corra un día.
 */
fun milisDeFechaSeleccionada(utcMillis: Long): Long =
    Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun milisParaSelectorFecha(millis: Long): Long =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()