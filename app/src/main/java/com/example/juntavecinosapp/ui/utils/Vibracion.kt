package com.example.juntavecinosapp.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Recurso nativo: vibración como retroalimentación háptica. */
object Vibracion {

    /** Un pulso corto: acción completada. */
    fun exito(context: Context) = vibrar(context, longArrayOf(0, 60))

    /** Dos pulsos cortos: el formulario tiene errores. */
    fun error(context: Context) = vibrar(context, longArrayOf(0, 80, 60, 80))

    private fun vibrar(context: Context, patron: LongArray) {
        val vibrador = obtenerVibrador(context) ?: return
        if (!vibrador.hasVibrator()) return
        vibrador.vibrate(VibrationEffect.createWaveform(patron, -1))
    }

    private fun obtenerVibrador(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
}