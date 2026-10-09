package com.example.juntavecinosapp.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

/**
 * Convierte fechas y horas a texto para guardarlas en Room, y las reconstruye al leerlas.
 * LocalDate se guarda como "2026-10-09" y LocalTime como "14:30".
 */
class Converters {

    @TypeConverter
    fun fechaATexto(fecha: LocalDate?): String? = fecha?.toString()

    @TypeConverter
    fun textoAFecha(texto: String?): LocalDate? = texto?.let { LocalDate.parse(it) }

    @TypeConverter
    fun horaATexto(hora: LocalTime?): String? = hora?.toString()

    @TypeConverter
    fun textoAHora(texto: String?): LocalTime? = texto?.let { LocalTime.parse(it) }
}