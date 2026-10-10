package com.example.juntavecinosapp.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.juntavecinosapp.ui.utils.ComprobanteFiles
import kotlinx.coroutines.launch

/**
 * Selector de comprobante: muestra la foto, o un recuadro vacío, y permite
 * tomarla con la cámara o elegirla de la galería.
 * No valida nada: recibe el error ya calculado por el ViewModel.
 */
@Composable
fun ImagenInteligente(
    uri: String?,
    error: String?,
    onUriChange: (String?) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Guarda la URI de la foto en curso: sobrevive a rotaciones mientras la cámara está abierta.
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    fun reemplazarFoto(nueva: String) {
        uri?.let { ComprobanteFiles.eliminar(context, Uri.parse(it)) }
        onUriChange(nueva)
    }

    // ---------- Cámara ----------
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val pendiente = uriPendiente
        uriPendiente = null
        if (pendiente != null) {
            if (exito) reemplazarFoto(pendiente)
            else ComprobanteFiles.eliminar(context, Uri.parse(pendiente))   // canceló: borra el archivo vacío
        }
    }

    fun abrirCamara() {
        val nueva = ComprobanteFiles.crearUriParaCamara(context)
        uriPendiente = nueva.toString()
        camara.launch(nueva)
    }

    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara()
        else onError("Sin permiso de cámara no se puede tomar la foto. Puedes elegirla desde la galería.")
    }

    // ---------- Galería (Photo Picker: no requiere permisos) ----------
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { origen ->
        if (origen != null) {
            scope.launch {
                val copia = ComprobanteFiles.copiarDesdeGaleria(context, origen)
                if (copia != null) reemplazarFoto(copia.toString())
                else onError("No se pudo cargar la imagen seleccionada. Intenta con otra.")
            }
        }
    }

    // ---------- Vista previa ----------
    val imagen by produceState<ImageBitmap?>(initialValue = null, key1 = uri) {
        value = uri?.let { ComprobanteFiles.cargarMiniatura(context, Uri.parse(it))?.asImageBitmap() }
    }

    val colorBorde by animateColorAsState(
        targetValue = if (error != null) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.outline,
        animationSpec = tween(300),
        label = "bordeComprobante"
    )

    Column(modifier = modifier) {
        Text(
            text = "Comprobante",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(if (error != null) 2.dp else 1.dp, colorBorde),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AnimatedContent(
                targetState = uri != null,
                transitionSpec = {
                    (fadeIn(tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300))) togetherWith
                            fadeOut(tween(150))
                },
                label = "vistaPreviaComprobante"
            ) { hayFoto ->
                if (hayFoto) {
                    val bitmap = imagen
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Foto del comprobante",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    } else {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = null,
                            tint = if (error != null) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Sin comprobante adjunto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Mensaje de error con ícono, animado al aparecer y desaparecer
        AnimatedVisibility(visible = error != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = error.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val permitido = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (permitido) abrirCamara()
                    else permisoCamara.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cámara")
            }

            OutlinedButton(
                onClick = {
                    galeria.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Galería")
            }

            AnimatedVisibility(visible = uri != null) {
                IconButton(onClick = {
                    uri?.let { ComprobanteFiles.eliminar(context, Uri.parse(it)) }
                    onUriChange(null)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Quitar foto",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}