package com.example.juntavecinosapp.ui.screens


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.juntavecinosapp.model.RolUsuario
import com.example.juntavecinosapp.ui.Rutas
import com.example.juntavecinosapp.ui.utils.TipoPantalla
import com.example.juntavecinosapp.ui.utils.tipoPantallaActual
import com.example.juntavecinosapp.viewmodel.AuthViewModel

// Una opción del menú principal: texto, ícono y la ruta a la que lleva.
private data class OpcionMenu(
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector,
    val ruta: String
)

// Qué opciones ve cada rol. Así el menú se arma solo según quién entró.
private fun opcionesPara(rol: RolUsuario): List<OpcionMenu> {
    val arriendos = listOf(
        OpcionMenu("Solicitar arriendo", "Reserva una dependencia", Icons.Filled.EventAvailable, Rutas.SOLICITAR_ARRIENDO),
        OpcionMenu("Disponibilidad", "Mira los arriendos y horarios", Icons.Filled.CalendarMonth, Rutas.DISPONIBILIDAD)
    )
    val transparencia = listOf(
        OpcionMenu("Transparencia", "Saldo y uso de los fondos", Icons.Filled.PieChart, Rutas.TRANSPARENCIA)
    )
    val tesoreria = listOf(
        OpcionMenu("Registrar pago", "Ingresa un pago recibido", Icons.Filled.AddCard, Rutas.REGISTRAR_PAGO),
        OpcionMenu("Registrar gasto", "Ingresa un gasto con comprobante", Icons.Filled.Paid, Rutas.REGISTRAR_GASTO),
        OpcionMenu("Historial", "Todos los movimientos", Icons.AutoMirrored.Filled.ReceiptLong, Rutas.HISTORIAL),
        OpcionMenu("Reporte", "Resumen por período", Icons.Filled.BarChart, Rutas.REPORTE)
    )
    return when (rol) {
        RolUsuario.VECINO -> arriendos + transparencia
        RolUsuario.DIRECTIVA -> arriendos + transparencia +
                OpcionMenu("Historial", "Todos los movimientos", Icons.AutoMirrored.Filled.ReceiptLong, Rutas.HISTORIAL) +
                OpcionMenu("Reporte", "Resumen por período", Icons.Filled.BarChart, Rutas.REPORTE)
        RolUsuario.TESORERIA -> tesoreria + transparencia + arriendos.last()
    }
}

private fun nombreDeRol(rol: RolUsuario): String = when (rol) {
    RolUsuario.VECINO -> "Vecino"
    RolUsuario.DIRECTIVA -> "Directiva"
    RolUsuario.TESORERIA -> "Tesorería"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onNavegar: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuarioActual.collectAsState()
    val tipoPantalla = tipoPantallaActual()

    // Si la sesión se cierra, volvemos al login.
    LaunchedEffect(usuario) {
        if (usuario == null) onCerrarSesion()
    }

    val actual = usuario ?: return
    val opciones = remember(actual.rol) { opcionesPara(actual.rol) }

    // Cuántas columnas caben según el tamaño de pantalla (diseño adaptable).
    val columnas = when (tipoPantalla) {
        TipoPantalla.COMPACTA -> 1
        TipoPantalla.MEDIANA -> 2
        TipoPantalla.EXPANDIDA -> 3
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Junta de Vecinos Zaror") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.cerrarSesion() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
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
            // El saludo ocupa todo el ancho de la grilla.
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Saludo(nombre = actual.nombre, rol = nombreDeRol(actual.rol))
            }
            items(opciones, key = { it.ruta }) { opcion ->
                TarjetaOpcion(opcion = opcion, onClick = { onNavegar(opcion.ruta) })
            }
        }
    }
}

@Composable
private fun Saludo(nombre: String, rol: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Hola, ${nombre.substringBefore(' ')}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ingresaste como $rol. ¿Qué quieres hacer hoy?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TarjetaOpcion(opcion: OpcionMenu, onClick: () -> Unit) {
    // Entrada suave: la tarjeta aparece subiendo un poquito.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = opcion.icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(0.dp).padding(start = 16.dp))
                Column {
                    Text(
                        text = opcion.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = opcion.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}