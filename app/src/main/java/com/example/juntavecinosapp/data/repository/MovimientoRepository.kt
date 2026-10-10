package com.example.juntavecinosapp.data.repository

import com.example.juntavecinosapp.data.local.MovimientoDao
import com.example.juntavecinosapp.model.Movimiento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MovimientoRepository(private val dao: MovimientoDao) {


    val movimientos: Flow<List<Movimiento>> = dao.obtenerTodos()

    fun movimientosPorTipo(tipo: String): Flow<List<Movimiento>> =
        dao.obtenerPorTipo(tipo)

    fun movimientosPorRango(desde: Long, hasta: Long): Flow<List<Movimiento>> =
        dao.obtenerPorRango(desde, hasta)

    suspend fun obtenerPorId(id: Int): Movimiento? = dao.obtenerPorId(id)


    val totalEntradas: Flow<Long> = dao.totalEntradas().map { it ?: 0L }

    val totalSalidas: Flow<Long> = dao.totalSalidas().map { it ?: 0L }

    fun totalPorTipo(tipo: String): Flow<Long> =
        dao.totalPorTipo(tipo).map { it ?: 0L }

    suspend fun insertar(movimiento: Movimiento): Long = dao.insertar(movimiento)

    suspend fun actualizar(movimiento: Movimiento) = dao.actualizar(movimiento)

    suspend fun eliminar(movimiento: Movimiento) = dao.eliminar(movimiento)
}