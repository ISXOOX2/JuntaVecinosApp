package com.example.juntavecinosapp.model

//Una dependencia de la sede que se puede arrendar. La tarifa es ficticia.
data class Dependencia(
    val nombre: String,
    val tarifaPorHora: Int   // en pesos
)

//Lista fija de dependencias disponibles (se usa en el selector de la pantalla).
object Dependencias {
    val lista = listOf(
        Dependencia("Salón multiuso", 12000),
        Dependencia("Sala de reuniones", 6000),
        Dependencia("Cancha techada", 9000)
    )

// Devuelve la tarifa por hora, o null si el nombre no existe.
    fun tarifaPorHora(nombre: String): Int? =
        lista.firstOrNull { it.nombre == nombre }?.tarifaPorHora
}