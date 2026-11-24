package com.example.juntavecinosapp.data.repository

import com.example.juntavecinosapp.data.local.ArriendoDao
import com.example.juntavecinosapp.data.model.EstadoRequest
import com.example.juntavecinosapp.data.model.aArriendo
import com.example.juntavecinosapp.data.model.aReservaDto
import com.example.juntavecinosapp.data.model.mensajeDeError
import com.example.juntavecinosapp.data.remote.RetrofitInstance
import com.example.juntavecinosapp.model.Arriendo
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime

/** Punto de acceso a los datos de arriendos: primero ms-reservas, luego Room. */
class ArriendoRepository(private val arriendoDao: ArriendoDao) {

    /** Lista de todos los arriendos; se actualiza sola cuando cambia la tabla. */
    fun observarTodos(): Flow<List<Arriendo>> = arriendoDao.observarTodos()

    /** Lista de los arriendos de un vecino. */
    fun observarPorUsuario(usuarioId: Int): Flow<List<Arriendo>> =
        arriendoDao.observarPorUsuario(usuarioId)

    /** Trae las reservas del servidor y las guarda en Room. Sin red: queda lo de Room. */
    suspend fun listar(usuarioId: Int? = null) {
        try {
            val remotas = RetrofitInstance.reservasApi.listar(usuarioId)
            remotas.forEach { arriendoDao.insertar(it.aArriendo()) }
        } catch (e: HttpException) {
            throw Exception(mensajeDeError(e))
        } catch (e: IOException) {
            // sin conexión: se muestra lo guardado en Room
        }
    }

    /** Crea la reserva en el servidor (calcula monto y revisa choques) y la guarda en Room. */
    suspend fun guardar(arriendo: Arriendo): Long {
        return try {
            val creada = RetrofitInstance.reservasApi.crear(arriendo.aReservaDto())
            arriendoDao.insertar(creada.aArriendo())
        } catch (e: HttpException) {
            throw Exception(mensajeDeError(e))   // ej: "Ese horario ya está ocupado en ..."
        } catch (e: IOException) {
            arriendoDao.insertar(arriendo)
        }
    }

    suspend fun actualizar(arriendo: Arriendo) = arriendoDao.actualizar(arriendo)

    suspend fun buscarPorId(id: Int): Arriendo? = arriendoDao.buscarPorId(id)

    /** Aprobar o rechazar una solicitud (lo hace la directiva). */
    suspend fun cambiarEstado(id: Int, estado: String) {
        try {
            val actualizada = RetrofitInstance.reservasApi.cambiarEstado(id, EstadoRequest(estado))
            arriendoDao.insertar(actualizada.aArriendo())
        } catch (e: HttpException) {
            throw Exception(mensajeDeError(e))
        } catch (e: IOException) {
            arriendoDao.cambiarEstado(id, estado)
        }
    }

    /** Elimina en el servidor y en Room. */
    suspend fun eliminar(id: Int) {
        try {
            RetrofitInstance.reservasApi.eliminar(id)
        } catch (e: HttpException) {
            // 404: no estaba en el servidor (se creó sin red), se borra igual de Room
            if (e.code() != 404) throw Exception(mensajeDeError(e))
        } catch (e: IOException) {
            // sin conexión: solo Room
        }
        arriendoDao.eliminarPorId(id)
    }

    /** True si ya hay un arriendo que choca con ese horario en esa dependencia. */
    suspend fun hayChoque(
        dependencia: String,
        fecha: LocalDate,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ): Boolean = arriendoDao.contarChoques(dependencia, fecha, horaInicio, horaFin) > 0
}
