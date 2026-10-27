package com.example.juntavecinosapp.ui.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** Recurso nativo: notificaciones locales. */
object Notificaciones {

    private const val CANAL_ID = "arriendos"

    private fun crearCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL_ID,
            "Arriendos",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(canal)
    }

    @SuppressLint("MissingPermission")
    fun solicitudEnviada(context: Context) {
        val permitido = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        if (!permitido) return

        crearCanal(context)
        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Solicitud enviada")
            .setContentText("Tu solicitud de arriendo quedó pendiente de revisión.")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1, notificacion)
    }
}