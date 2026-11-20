package com.example.juntavecinosapp.data.remote

import com.example.juntavecinosapp.data.model.MovimientoDto
import com.example.juntavecinosapp.data.model.ResumenDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// ms-finanzas (puerto 9093)
interface FinanzasApiService {
    @GET("movimientos")
    suspend fun listar(): List<MovimientoDto>

    @POST("movimientos")
    suspend fun crear(@Body movimiento: MovimientoDto): MovimientoDto

    @DELETE("movimientos/{id}")
    suspend fun eliminar(@Path("id") id: Int)

    @GET("movimientos/resumen")
    suspend fun resumen(): ResumenDto
}
