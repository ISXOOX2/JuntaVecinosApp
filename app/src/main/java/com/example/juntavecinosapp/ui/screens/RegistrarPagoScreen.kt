package com.example.juntavecinosapp.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import com.example.juntavecinosapp.model.TipoMovimiento
import com.example.juntavecinosapp.ui.components.RegistroMovimientoContenido
import com.example.juntavecinosapp.viewmodel.MovimientoViewModel

@Composable
fun RegistrarPagoScreen(
    viewModel: MovimientoViewModel,
    onVolver: () -> Unit
) {
    RegistroMovimientoContenido(
        titulo = "Registrar pago o ingreso",
        tipoInicial = TipoMovimiento.PAGO,
        etiquetaPersona = "Vecino o aportante",
        iconoPersona = Icons.Filled.Person,
        mostrarSelectorTipo = true,
        viewModel = viewModel,
        onVolver = onVolver
    )
}