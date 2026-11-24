package com.example.juntavecinosapp.data.remote

import com.example.juntavecinosapp.data.model.EstadoRequest
import com.example.juntavecinosapp.data.model.ReservaDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ms-reservas (puerto 9092)
interface ReservasApiService {
    // usuarioId null = todas las reservas
    @GET("reservas")
    suspend fun listar(@Query("usuarioId") usuarioId: Int?): List<ReservaDto>

    @POST("reservas")
    suspend fun crear(@Body reserva: ReservaDto): ReservaDto

    @PATCH("reservas/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Int, @Body estado: EstadoRequest): ReservaDto

    @DELETE("reservas/{id}")
    suspend fun eliminar(@Path("id") id: Int)
}
