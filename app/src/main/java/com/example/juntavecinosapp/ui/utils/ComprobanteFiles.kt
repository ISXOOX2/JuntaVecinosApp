package com.example.juntavecinosapp.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/**
 * Manejo de los archivos de comprobantes (fotos de cámara o galería).
 * Todo se guarda en filesDir/comprobantes, privado de esta app.
 */
object ComprobanteFiles {

    private const val CARPETA = "comprobantes"

    private fun autoridad(context: Context) = "${context.packageName}.fileprovider"

    private fun carpeta(context: Context): File =
        File(context.filesDir, CARPETA).apply { mkdirs() }

    private fun nuevoArchivo(context: Context): File =
        File(carpeta(context), "comprobante_${System.currentTimeMillis()}.jpg")

    /** URI vacía donde la cámara escribirá la foto. */
    fun crearUriParaCamara(context: Context): Uri =
        FileProvider.getUriForFile(context, autoridad(context), nuevoArchivo(context))

    /** Copia la imagen elegida en la galería a la carpeta privada. Devuelve null si falla. */
    suspend fun copiarDesdeGaleria(context: Context, origen: Uri): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val destino = nuevoArchivo(context)
                context.contentResolver.openInputStream(origen)?.use { entrada ->
                    destino.outputStream().use { salida -> entrada.copyTo(salida) }
                } ?: return@withContext null
                FileProvider.getUriForFile(context, autoridad(context), destino)
            } catch (e: Exception) {
                null
            }
        }

    /** Borra un comprobante propio (foto cancelada, reemplazada o quitada). */
    fun eliminar(context: Context, uri: Uri) {
        val nombre = File(uri.lastPathSegment ?: return).name
        File(carpeta(context), nombre).takeIf { it.exists() }?.delete()
    }

    /** Carga una versión liviana de la imagen (evita agotar memoria con fotos de 12 MP). */
    suspend fun cargarMiniatura(context: Context, uri: Uri, anchoMax: Int = 1024): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, limites)
                }
                var muestra = 1
                while (limites.outWidth / muestra > anchoMax) muestra *= 2

                val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
                val bitmap = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opciones)
                } ?: return@withContext null

                val grados = context.contentResolver.openInputStream(uri)?.use { leerRotacion(it) } ?: 0
                if (grados == 0) bitmap
                else Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height,
                    Matrix().apply { postRotate(grados.toFloat()) }, true
                )
            } catch (e: Exception) {
                null
            }
        }

    private fun leerRotacion(entrada: InputStream): Int =
        when (ExifInterface(entrada).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
}