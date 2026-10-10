package com.example.juntavecinosapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.juntavecinosapp.model.Arriendo
import com.example.juntavecinosapp.model.Usuario
import com.example.juntavecinosapp.model.Movimiento

//Base de datos local de la app (Room / SQLite).
//Cada entidad de la lista es una tabla. Si se agrega una entidad nueva
//(ej Movimiento), hay que sumarla aquí y subir el número de versión.

@Database(
    entities = [Usuario::class, Arriendo::class, Movimiento::class],  // 2. suma Movimiento
    version = 2,                                                       // 3. sube de 1 a 2
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun arriendoDao(): ArriendoDao
    abstract fun movimientoDao(): MovimientoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Devuelve siempre la misma base de datos (se crea solo la primera vez). */
        fun obtener(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "junta_vecinos.db"
                )
                    // Mientras desarrollamos: si cambia la estructura, se recrea la base.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) = sembrarUsuariosDePrueba(db)
                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) =
                            sembrarUsuariosDePrueba(db)
                    })
                    .build()
                    .also { INSTANCE = it }
            }

        }
        // Usuarios ficticios para probar cada rol. Solo se insertan si el correo no existe.
        private val USUARIOS_DE_PRUEBA = listOf(
            Triple("Vecino Prueba", "vecino@junta.cl", "vecino123" to "VECINO"),
            Triple("Directiva Prueba", "directiva@junta.cl", "directiva123" to "DIRECTIVA"),
            Triple("Tesorería Prueba", "tesoreria@junta.cl", "tesoreria123" to "TESORERIA")
        )

        private fun sembrarUsuariosDePrueba(db: SupportSQLiteDatabase) {
            USUARIOS_DE_PRUEBA.forEach { (nombre, correo, claveYRol) ->
                db.execSQL(
                    "INSERT INTO usuarios (nombre, correo, clave, rol) " +
                            "SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = ?)",
                    arrayOf(nombre, correo, claveYRol.first, claveYRol.second, correo)
                )
            }
        }
    }
}