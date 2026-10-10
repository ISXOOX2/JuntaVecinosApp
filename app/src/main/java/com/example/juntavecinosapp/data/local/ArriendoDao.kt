package com.example.juntavecinosapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.juntavecinosapp.model.Arriendo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

/** Consultas de la tabla "arriendos". */
@Dao
interface ArriendoDao {

    @Insert
    suspend fun insertar(arriendo: Arriendo): Long

    @Update
    suspend fun actualizar(arriendo: Arriendo)

    /** Todos los arriendos, ordenados por fecha y hora. Se actualiza solo si cambia la tabla. */
    @Query("SELECT * FROM arriendos ORDER BY fecha, horaInicio")
    fun observarTodos(): Flow<List<Arriendo>>

    /** Solo los arriendos de un vecino. */
    @Query("SELECT * FROM arriendos WHERE usuarioId = :usuarioId ORDER BY fecha DESC, horaInicio")
    fun observarPorUsuario(usuarioId: Int): Flow<List<Arriendo>>

    @Query("SELECT * FROM arriendos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Arriendo?

    /** Aprobar o rechazar una solicitud (lo hace la directiva). */
    @Query("UPDATE arriendos SET estado = :estado WHERE id = :id")
    suspend fun cambiarEstado(id: Int, estado: String)

    /**
     * Cuenta cuántos arriendos no rechazados chocan con ese horario
     * en la misma dependencia y fecha. Si da 0, el horario está libre.
     */
    @Query(
        """
        SELECT COUNT(*) FROM arriendos
        WHERE dependencia = :dependencia
          AND fecha = :fecha
          AND estado != 'RECHAZADO'
          AND horaInicio < :horaFin
          AND horaFin > :horaInicio
        """
    )
    suspend fun contarChoques(
        dependencia: String,
        fecha: LocalDate,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ): Int
}