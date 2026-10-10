package com.example.juntavecinosapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.juntavecinosapp.model.Arriendo
import com.example.juntavecinosapp.model.EstadoArriendo
import com.example.juntavecinosapp.ui.utils.formatearPesos
import java.time.format.DateTimeFormatter

private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

// Tarjeta de un arriendo. Si onClick es null, la tarjeta no se puede tocar.
// verDetalles = false oculta motivo y monto (para arriendos de otros vecinos).
@Composable
fun TarjetaArriendo(
    arriendo: Arriendo,
    esMio: Boolean,
    verDetalles: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val clic = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(clic),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = arriendo.dependencia,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${arriendo.fecha.format(formatoFecha)} · " +
                            "${arriendo.horaInicio.format(formatoHora)} - ${arriendo.horaFin.format(formatoHora)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (verDetalles) {
                    Text(
                        text = arriendo.motivo,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatearPesos(arriendo.monto.toLong()),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (esMio) {
                    Text(
                        text = "Tu solicitud",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            EstadoChip(estado = arriendo.estado)
        }
    }
}

// Etiqueta de color según el estado; el color cambia con animación si el estado cambia.
@Composable
fun EstadoChip(estado: String, modifier: Modifier = Modifier) {
    val (color, texto) = when (estado) {
        EstadoArriendo.APROBADO -> MaterialTheme.colorScheme.secondary to "Aprobado"
        EstadoArriendo.RECHAZADO -> MaterialTheme.colorScheme.error to "Rechazado"
        else -> MaterialTheme.colorScheme.tertiary to "Pendiente"
    }
    val colorAnimado = animateColorAsState(targetValue = color, label = "colorEstado").value

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = colorAnimado.copy(alpha = 0.15f)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = colorAnimado
        )
    }
}