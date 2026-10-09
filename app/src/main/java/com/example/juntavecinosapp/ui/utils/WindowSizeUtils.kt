package com.example.juntavecinosapp.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Tipos de pantalla de la app, según el ancho disponible.
 * Usa los mismos cortes que Material 3 (Window Size Classes):
 * menos de 600 dp = compacta (celular), 600 a 839 dp = mediana, 840 dp o más = expandida.
 */
enum class TipoPantalla { COMPACTA, MEDIANA, EXPANDIDA }

/**
 * Devuelve el tipo de pantalla actual. Se recalcula solo si el ancho cambia,
 * por ejemplo al rotar el dispositivo.
 */
@Composable
fun tipoPantallaActual(): TipoPantalla {
    val anchoDp = LocalConfiguration.current.screenWidthDp
    return when {
        anchoDp < 600 -> TipoPantalla.COMPACTA
        anchoDp < 840 -> TipoPantalla.MEDIANA
        else -> TipoPantalla.EXPANDIDA
    }

}