package com.example.juntavecinosapp.data.repository

import com.example.juntavecinosapp.data.model.Indicadores
import com.example.juntavecinosapp.data.remote.RetrofitInstance

// UF y dólar desde mindicador.cl (igual que PostRepository del profe)
class IndicadoresRepository {
    suspend fun obtenerIndicadores(): Indicadores {
        return RetrofitInstance.indicadoresApi.obtenerIndicadores()
    }
}
