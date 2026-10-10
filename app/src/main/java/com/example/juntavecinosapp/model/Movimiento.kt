package com.example.juntavecinosapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


object TipoMovimiento {
    const val PAGO = "PAGO"         // Pago de gasto común / cuota de un vecino
    const val INGRESO = "INGRESO"   // Ingreso general (donación, evento, etc.)
    const val GASTO = "GASTO"       // Gasto de la junta
}

@Entity(tableName = "movimientos")
data class Movimiento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "tipo")
    val tipo: String,

    @ColumnInfo(name = "concepto")
    val concepto: String,

    @ColumnInfo(name = "monto")
    val monto: Long,

    @ColumnInfo(name = "fecha")
    val fecha: Long,

    @ColumnInfo(name = "persona")
    val persona: String = "",

    @ColumnInfo(name = "descripcion")
    val descripcion: String = "",

    @ColumnInfo(name = "uriComprobante")
    val uriComprobante: String? = null
)