package com.example.juntavecinosapp.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.runtime.Composable
import com.example.juntavecinosapp.model.TipoMovimiento
import com.example.juntavecinosapp.ui.components.RegistroMovimientoContenido
import com.example.juntavecinosapp.viewmodel.MovimientoViewModel

@Composable
fun RegistrarGastoScreen(
    viewModel: MovimientoViewModel,
    onVolver: () -> Unit
) {
    RegistroMovimientoContenido(
        titulo = "Registrar gasto",
        tipoInicial = TipoMovimiento.GASTO,
        etiquetaPersona = "Proveedor",
        iconoPersona = Icons.Filled.Storefront,
        mostrarSelectorTipo = false,
        viewModel = viewModel,
        onVolver = onVolver
    )
}