package com.example.juntavecinosapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.juntavecinosapp.model.EstadoArriendo
import com.example.juntavecinosapp.model.RolUsuario
import com.example.juntavecinosapp.ui.components.TarjetaArriendo
import com.example.juntavecinosapp.ui.utils.TipoPantalla
import com.example.juntavecinosapp.ui.utils.tipoPantallaActual
import com.example.juntavecinosapp.viewmodel.ArriendoViewModel

private const val FILTRO_TODOS = "TODOS"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisponibilidadScreen(
    viewModel: ArriendoViewModel,
    usuarioId: Int,
    rol: String,
    onVerDetalle: (Int) -> Unit,
    onVolver: () -> Unit
) {
    val todos by viewModel.todos.collectAsState()
    var filtro by rememberSaveable { mutableStateOf(FILTRO_TODOS) }
    val tipoPantalla = tipoPantallaActual()

    val columnas = when (tipoPantalla) {
        TipoPantalla.COMPACTA -> 1
        TipoPantalla.MEDIANA -> 2
        TipoPantalla.EXPANDIDA -> 3
    }

    val puedeVerTodo = rol == RolUsuario.DIRECTIVA

    // Un vecino no ve las solicitudes rechazadas de otros, solo las suyas.
    val visibles = todos
        .filter { rol != RolUsuario.VECINO || it.estado != EstadoArriendo.RECHAZADO || it.usuarioId == usuarioId }
        .filter { filtro == FILTRO_TODOS || it.estado == filtro }
        .sortedWith(compareBy({ it.fecha }, { it.horaInicio }))

    val filtros = listOf(
        FILTRO_TODOS to "Todos",
        EstadoArriendo.PENDIENTE to "Pendientes",
        EstadoArriendo.APROBADO to "Aprobados",
        EstadoArriendo.RECHAZADO to "Rechazados"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disponibilidad") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnas),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filtros por estado, ocupan todo el ancho.
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtros) { (valor, texto) ->
                        FilterChip(
                            selected = filtro == valor,
                            onClick = { filtro = valor },
                            label = { Text(texto) }
                        )
                    }
                }
            }

            if (visibles.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay arriendos para mostrar",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(visibles, key = { it.id }) { arriendo ->
                    val esMio = arriendo.usuarioId == usuarioId
                    val puedeAbrir = esMio || puedeVerTodo
                    TarjetaArriendo(
                        arriendo = arriendo,
                        esMio = esMio,
                        verDetalles = puedeAbrir,
                        onClick = if (puedeAbrir) ({ onVerDetalle(arriendo.id) }) else null
                    )
                }
            }
        }
    }
}