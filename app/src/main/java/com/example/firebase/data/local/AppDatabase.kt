package com.example.firebase.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// @Database le dice qué tablas incluir (entities = [ArtistEntity::class]) y qué versión es (versión 1).
@Database(entities = [ArtistEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Le decimos a la base de datos que tiene disponible el manual de instrucciones del Artista.
    abstract fun artistDao(): ArtistDao

    // "companion object" sirve para crear un Singleton (igual que en Retrofit).
    // Nos asegura que solo se crea UNA base de datos en toda la aplicación.
    companion object {

        // @Volatile significa que cualquier hilo (proceso) de la app sabrá al instante si la base de datos ya está creada.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Función que pide la base de datos.
        fun getDatabase(context: Context): AppDatabase {

            // Si ya existe (INSTANCE no es null), nos la da directamente.
            // Si no existe, entra en el bloque "synchronized" para crearla por primera vez.
            return INSTANCE ?: synchronized(this) {

                // Room.databaseBuilder es el constructor oficial de Android.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soundconnect_database" // Este es el nombre del archivo real que se guarda oculta en tu móvil.
                ).build()

                // Guardamos la base de datos recién creada y la devolvemos.
                INSTANCE = instance
                instance
            }
        }
    }
}