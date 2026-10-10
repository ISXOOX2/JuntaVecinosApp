package com.example.juntavecinosapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext                       // NUEVO
import androidx.lifecycle.viewmodel.compose.viewModel                  // NUEVO
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.juntavecinosapp.data.local.AppDatabase              // NUEVO
import com.example.juntavecinosapp.data.repository.MovimientoRepository // NUEVO
import com.example.juntavecinosapp.ui.screens.RegistrarGastoScreen     // NUEVO
import com.example.juntavecinosapp.ui.screens.RegistrarPagoScreen      // NUEVO
import com.example.juntavecinosapp.viewmodel.MovimientoViewModel       // NUEVO
import com.example.juntavecinosapp.viewmodel.MovimientoViewModelFactory // NUEVO

// Nombres de las rutas de navegación de la app.
//Se usan siempre desde aquí para no escribir textos sueltos en las pantallas.

object Rutas {
    // Isa: arriendos y acceso
    const val LOGIN = "login"
    const val HOME = "home"
    const val SOLICITAR_ARRIENDO = "solicitar_arriendo"
    const val DISPONIBILIDAD = "disponibilidad"
    const val DETALLE_ARRIENDO = "detalle_arriendo"

    // Andy- finanzas
    const val REGISTRAR_PAGO = "registrar_pago"
    const val REGISTRAR_GASTO = "registrar_gasto"
    const val HISTORIAL = "historial"
    const val TRANSPARENCIA = "transparencia"
    const val REPORTE = "reporte"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    // NUEVO: un solo ViewModel de finanzas, compartido por todas las pantallas de Andy
    val context = LocalContext.current
    val movimientoViewModel: MovimientoViewModel = viewModel(
        factory = MovimientoViewModelFactory(
            MovimientoRepository(AppDatabase.obtener(context).movimientoDao())
        )
    )

    NavHost(
        navController = navController,
        startDestination = Rutas.REGISTRAR_PAGO      // <- AQUÍ se cambia la pantalla de inicio (ver abajo)
    ) {
        // Isa
        composable(Rutas.LOGIN) { PantallaProvisoria("Login / Registro") }
        composable(Rutas.HOME) { PantallaProvisoria("Home") }
        composable(Rutas.SOLICITAR_ARRIENDO) { PantallaProvisoria("Solicitar arriendo") }
        composable(Rutas.DISPONIBILIDAD) { PantallaProvisoria("Disponibilidad") }
        composable(Rutas.DETALLE_ARRIENDO) { PantallaProvisoria("Detalle de arriendo") }

        // Andy
        composable(Rutas.REGISTRAR_PAGO) {                                  // NUEVO
            RegistrarPagoScreen(
                viewModel = movimientoViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.REGISTRAR_GASTO) {                                 // NUEVO
            RegistrarGastoScreen(
                viewModel = movimientoViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.HISTORIAL) { PantallaProvisoria("Historial de movimientos") }
        composable(Rutas.TRANSPARENCIA) { PantallaProvisoria("Transparencia") }
        composable(Rutas.REPORTE) { PantallaProvisoria("Reporte") }
    }
}

@Composable
private fun PantallaProvisoria(titulo: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = titulo, style = MaterialTheme.typography.headlineMedium)
    }
}