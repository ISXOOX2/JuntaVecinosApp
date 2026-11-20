package com.example.juntavecinosapp.data.remote

import com.example.juntavecinosapp.data.model.LoginRequest
import com.example.juntavecinosapp.data.model.RegistroRequest
import com.example.juntavecinosapp.data.model.UsuarioDto
import retrofit2.http.Body
import retrofit2.http.POST

// ms-admin (puerto 9091)
interface AdminApiService {
    @POST("auth/register")
    suspend fun registrar(@Body datos: RegistroRequest): UsuarioDto

    @POST("auth/login")
    suspend fun login(@Body datos: LoginRequest): UsuarioDto
}
