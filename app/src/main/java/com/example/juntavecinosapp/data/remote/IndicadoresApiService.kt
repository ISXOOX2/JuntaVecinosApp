package com.example.juntavecinosapp.data.remote

import com.example.juntavecinosapp.data.model.Indicadores
import retrofit2.http.GET

// API externa mindicador.cl (UF y dólar)
interface IndicadoresApiService {
    @GET("api")
    suspend fun obtenerIndicadores(): Indicadores
}
