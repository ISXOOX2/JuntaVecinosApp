package com.example.juntavecinosapp.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.juntavecinosapp.model.Movimiento
import com.example.juntavecinosapp.model.TipoMovimiento
import com.example.juntavecinosapp.ui.components.TarjetaMovimiento
import com.example.juntavecinosapp.ui.utils.ComprobanteFiles
import com.example.juntavecinosapp.viewmodel.MovimientoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    viewModel: MovimientoViewModel,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val movimientos by viewModel.movimientos.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()

    // Solo estado visual: cuál tarjeta espera confirmación para eliminarse.
    var porEliminar by remember { mutableStateOf<Movimiento?>(null) }

    val opciones = listOf(
        null to "Todos",
        TipoMovimiento.PAGO to "Pagos",
        TipoMovimiento.INGRESO to "Ingresos",
        TipoMovimiento.GASTO to "Gastos"
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Historial de movimientos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                windowInsets = WindowInsets(0)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(opciones) { (tipo, texto) ->
                    val seleccionado = filtro == tipo
                    FilterChip(
                        selected = seleccionado,
                        onClick = { viewModel.filtrarPor(tipo) },
                        label = { Text(texto) },
                        leadingIcon = if (seleccionado) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                AnimatedContent(
                    targetState = movimientos.isEmpty(),
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "listaOVacio"
                ) { vacio ->
                    if (vacio) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (filtro == null) "Aún no hay movimientos registrados"
                                else "No hay movimientos de este tipo",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.widthIn(max = 700.dp).fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(movimientos, key = { it.id }) { movimiento ->
                                TarjetaMovimiento(
                                    movimiento = movimiento,
                                    onEliminar = { porEliminar = movimiento },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    porEliminar?.let { movimiento ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar movimiento") },
            text = { Text("¿Seguro que quieres eliminar \"${movimiento.concepto}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    movimiento.uriComprobante?.let { ComprobanteFiles.eliminar(context, Uri.parse(it)) }
                    viewModel.eliminar(movimiento)
                    porEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}