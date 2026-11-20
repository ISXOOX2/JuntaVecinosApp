package com.example.juntavecinosapp.data.model

// Respuesta de https://mindicador.cl/api (solo lo que usamos)
data class Indicadores(
    val uf: IndicadorValor,
    val dolar: IndicadorValor
)

data class IndicadorValor(
    val valor: Double
)
