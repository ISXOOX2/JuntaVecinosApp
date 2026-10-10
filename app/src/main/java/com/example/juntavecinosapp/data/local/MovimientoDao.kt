package com.example.juntavecinosapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.juntavecinosapp.model.Movimiento
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(movimiento: Movimiento): Long

    @Update
    suspend fun actualizar(movimiento: Movimiento)

    @Delete
    suspend fun eliminar(movimiento: Movimiento)

    @Query("SELECT * FROM movimientos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Movimiento?


    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<Movimiento>>

    @Query("SELECT * FROM movimientos WHERE tipo = :tipo ORDER BY fecha DESC")
    fun obtenerPorTipo(tipo: String): Flow<List<Movimiento>>

    @Query(
        "SELECT * FROM movimientos " +
                "WHERE fecha BETWEEN :desde AND :hasta ORDER BY fecha DESC"
    )
    fun obtenerPorRango(desde: Long, hasta: Long): Flow<List<Movimiento>>


    @Query("SELECT SUM(monto) FROM movimientos WHERE tipo = :tipo")
    fun totalPorTipo(tipo: String): Flow<Long?>

    @Query("SELECT SUM(monto) FROM movimientos WHERE tipo IN ('PAGO', 'INGRESO')")
    fun totalEntradas(): Flow<Long?>

    @Query("SELECT SUM(monto) FROM movimientos WHERE tipo = 'GASTO'")
    fun totalSalidas(): Flow<Long?>
}