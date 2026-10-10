package com.example.juntavecinosapp.data.repository

import com.example.juntavecinosapp.data.local.ArriendoDao
import com.example.juntavecinosapp.model.Arriendo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

/** Punto de acceso a los datos de arriendos. */
class ArriendoRepository(private val arriendoDao: ArriendoDao) {

    /** Lista de todos los arriendos; se actualiza sola cuando cambia la tabla. */
    fun observarTodos(): Flow<List<Arriendo>> = arriendoDao.observarTodos()

    /** Lista de los arriendos de un vecino. */
    fun observarPorUsuario(usuarioId: Int): Flow<List<Arriendo>> =
        arriendoDao.observarPorUsuario(usuarioId)

    suspend fun guardar(arriendo: Arriendo): Long = arriendoDao.insertar(arriendo)

    suspend fun actualizar(arriendo: Arriendo) = arriendoDao.actualizar(arriendo)

    suspend fun buscarPorId(id: Int): Arriendo? = arriendoDao.buscarPorId(id)

    /** Aprobar o rechazar una solicitud (lo hace la directiva). */
    suspend fun cambiarEstado(id: Int, estado: String) =
        arriendoDao.cambiarEstado(id, estado)

    /** True si ya hay un arriendo que choca con ese horario en esa dependencia. */
    suspend fun hayChoque(
        dependencia: String,
        fecha: LocalDate,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ): Boolean = arriendoDao.contarChoques(dependencia, fecha, horaInicio, horaFin) > 0
}