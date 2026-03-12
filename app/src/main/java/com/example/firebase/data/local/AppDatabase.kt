package com.example.firebase.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ArtistEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // función abstracta que devuelve el dao
    abstract fun artistDao(): ArtistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // esta función se llama para pedir la base de datos
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                //usamos synchronized por si dos partes de la app quieren acceder a la vez solo pueda hacerlo una

                // se contruye la base de datos
                val instance = Room.databaseBuilder(
                    context.applicationContext, // usa el contexto global para que no haya fugas de memoria
                    AppDatabase::class.java,    // la clase de la base de datos
                    "soundconnect_database"     // el nombre del archivo fisico
                ).build()

                // guardamos las base de datos en la variable
                INSTANCE = instance

                // devuelve la instancia para quien la tenga que usar
                instance
            }
        }
    }
}