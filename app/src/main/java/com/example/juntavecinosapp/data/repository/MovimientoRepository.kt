package com.example.juntavecinosapp.data.repository

import com.example.juntavecinosapp.data.local.MovimientoDao
import com.example.juntavecinosapp.data.model.ResumenDto
import com.example.juntavecinosapp.data.model.aMovimiento
import com.example.juntavecinosapp.data.model.aMovimientoDto
import com.example.juntavecinosapp.data.model.mensajeDeError
import com.example.juntavecinosapp.data.remote.RetrofitInstance
import com.example.juntavecinosapp.model.Movimiento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

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

    /** Trae los movimientos de ms-finanzas y los guarda en Room. Sin red: queda lo de Room. */
    suspend fun listar() {
        try {
            val remotos = RetrofitInstance.finanzasApi.listar()
            remotos.forEach { dao.insertar(it.aMovimiento()) }
        } catch (e: HttpException) {
            throw Exception(mensajeDeError(e))
        } catch (e: IOException) {
            // sin conexión: se muestra lo guardado en Room
        }
    }

    /** Guarda en el servidor y luego en Room con el id del servidor. Sin red: solo Room. */
    suspend fun insertar(movimiento: Movimiento): Long {
        return try {
            val creado = RetrofitInstance.finanzasApi.crear(movimiento.aMovimientoDto())
            dao.insertar(creado.aMovimiento())
        } catch (e: HttpException) {
            throw Exception(mensajeDeError(e))
        } catch (e: IOException) {
            dao.insertar(movimiento)
        }
    }

    suspend fun actualizar(movimiento: Movimiento) = dao.actualizar(movimiento)

    /** Elimina en el servidor y en Room. */
    suspend fun eliminar(movimiento: Movimiento) {
        try {
            RetrofitInstance.finanzasApi.eliminar(movimiento.id)
        } catch (e: HttpException) {
            // 404: no estaba en el servidor (se creó sin red), se borra igual de Room
            if (e.code() != 404) throw Exception(mensajeDeError(e))
        } catch (e: IOException) {
            // sin conexión: solo Room
        }
        dao.eliminar(movimiento)
    }

    /** Totales calculados por el servidor. */
    suspend fun resumen(): ResumenDto = RetrofitInstance.finanzasApi.resumen()
}
