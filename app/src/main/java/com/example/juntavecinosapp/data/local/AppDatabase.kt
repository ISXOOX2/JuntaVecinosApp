package com.example.juntavecinosapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}