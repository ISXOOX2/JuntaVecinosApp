package com.example.juntavecinosapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.juntavecinosapp.data.local.AppDatabase
import com.example.juntavecinosapp.data.repository.MovimientoRepository
import com.example.juntavecinosapp.ui.screens.HistorialScreen
import com.example.juntavecinosapp.ui.screens.RegistrarGastoScreen
import com.example.juntavecinosapp.ui.screens.RegistrarPagoScreen
import com.example.juntavecinosapp.ui.screens.ReporteScreen
import com.example.juntavecinosapp.ui.screens.TransparenciaScreen
import com.example.juntavecinosapp.viewmodel.MovimientoViewModel
import com.example.juntavecinosapp.viewmodel.MovimientoViewModelFactory
import com.example.juntavecinosapp.viewmodel.ReporteViewModel
import com.example.juntavecinosapp.viewmodel.ReporteViewModelFactory
import com.example.juntavecinosapp.data.repository.UsuarioRepository
import com.example.juntavecinosapp.ui.screens.LoginScreen
import com.example.juntavecinosapp.viewmodel.AuthViewModel
import com.example.juntavecinosapp.viewmodel.AuthViewModelFactory

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
    // Un solo ViewModel de finanzas, compartido por todas las pantallas de Andy
    val context = LocalContext.current
    val repositorioMovimientos = remember {
        MovimientoRepository(AppDatabase.obtener(context).movimientoDao())
    }
    val movimientoViewModel: MovimientoViewModel = viewModel(
        factory = MovimientoViewModelFactory(repositorioMovimientos)
    )
    val reporteViewModel: ReporteViewModel = viewModel(
        factory = ReporteViewModelFactory(repositorioMovimientos)
    )
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            UsuarioRepository(AppDatabase.obtener(context).usuarioDao())
        )
    )

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
    ) {
        // Isa
        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onAccesoExitoso = {
                    // al entrar se borra el login del historial para que "atrás" no vuelva a él
                    navController.navigate(Rutas.HOME) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.HOME) { PantallaProvisoria("Home") }
        composable(Rutas.SOLICITAR_ARRIENDO) { PantallaProvisoria("Solicitar arriendo") }
        composable(Rutas.DISPONIBILIDAD) { PantallaProvisoria("Disponibilidad") }
        composable(Rutas.DETALLE_ARRIENDO) { PantallaProvisoria("Detalle de arriendo") }

        // Andy
        composable(Rutas.REGISTRAR_PAGO) {
            RegistrarPagoScreen(
                viewModel = movimientoViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.REGISTRAR_GASTO) {
            RegistrarGastoScreen(
                viewModel = movimientoViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.HISTORIAL) {
            HistorialScreen(
                viewModel = movimientoViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.TRANSPARENCIA) {
            TransparenciaScreen(
                viewModel = movimientoViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.REPORTE) {
            ReporteScreen(
                viewModel = reporteViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
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