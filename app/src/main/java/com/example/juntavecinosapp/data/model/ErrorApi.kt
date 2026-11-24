package com.example.juntavecinosapp.data.model

import com.google.gson.Gson
import retrofit2.HttpException

// JSON de error de los microservicios: {timestamp,status,error,message,path}
data class ErrorApi(
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null
)

// Saca el "message" del error del servidor (ej: "Ese horario ya está ocupado en ...")
fun mensajeDeError(e: HttpException): String {
    return try {
        val cuerpo = e.response()?.errorBody()?.string()
        Gson().fromJson(cuerpo, ErrorApi::class.java)?.message ?: "Error del servidor (${e.code()})"
    } catch (ex: Exception) {
        "Error del servidor (${e.code()})"
    }
}
