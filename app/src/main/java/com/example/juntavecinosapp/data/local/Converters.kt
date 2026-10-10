package com.example.juntavecinosapp.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

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