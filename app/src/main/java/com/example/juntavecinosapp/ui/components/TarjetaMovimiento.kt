package com.example.juntavecinosapp.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.juntavecinosapp.model.Movimiento
import com.example.juntavecinosapp.model.TipoMovimiento
import com.example.juntavecinosapp.ui.utils.ComprobanteFiles
import com.example.juntavecinosapp.ui.utils.formatearFecha
import com.example.juntavecinosapp.ui.utils.formatearPesos

private val VerdeIngreso = Color(0xFF2E7D32)

@Composable
fun TarjetaMovimiento(
    movimiento: Movimiento,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expandida by rememberSaveable(movimiento.id) { mutableStateOf(false) }

    val esGasto = movimiento.tipo == TipoMovimiento.GASTO
    val colorMonto = if (esGasto) MaterialTheme.colorScheme.error else VerdeIngreso
    val signo = if (esGasto) "-" else "+"

    val (icono: ImageVector, etiqueta: String) = when (movimiento.tipo) {
        TipoMovimiento.PAGO -> Icons.Filled.Payments to "Pago"
        TipoMovimiento.INGRESO -> Icons.Filled.Savings to "Ingreso"
        else -> Icons.Filled.ShoppingCart to "Gasto"
    }

    // La foto se carga solo cuando la tarjeta se expande.
    val miniatura by produceState<ImageBitmap?>(
        initialValue = null,
        movimiento.uriComprobante,
        expandida
    ) {
        val uri = movimiento.uriComprobante
        if (expandida && uri != null && value == null) {
            value = ComprobanteFiles.cargarMiniatura(context, Uri.parse(uri), 700)?.asImageBitmap()
        }
    }

    Card(
        onClick = { expandida = !expandida },
        modifier = modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = colorMonto.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icono, contentDescription = etiqueta, tint = colorMonto)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movimiento.concepto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$etiqueta · ${formatearFecha(movimiento.fecha)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (movimiento.persona.isNotBlank()) {
                        Text(
                            text = movimiento.persona,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$signo ${formatearPesos(movimiento.monto)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorMonto
                    )
                    if (movimiento.uriComprobante != null) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = "Tiene comprobante",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expandida) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (movimiento.descripcion.isNotBlank()) {
                        Text(
                            text = movimiento.descripcion,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    miniatura?.let { bitmap ->
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Comprobante",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onEliminar) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}