package com.example.firebase.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// @Database le indica a Android que esta clase es una base de datos de Room.
// 'entities = [ArtistEntity::class]' le dice qué tablas (entidades) va a tener dentro.
// 'version = 1' es la versión de la BD (si mañana añadieras otra tabla, tendrías que poner version = 2).
// 'exportSchema = false' es para que no guarde un historial de las versiones en un archivo extra (no nos hace falta aquí).
@Database(entities = [ArtistEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() { // Tiene que ser una clase 'abstract' que herede de RoomDatabase

    // Definimos una función abstracta que nos devuelve el DAO (Data Access Object).
    // El DAO es el archivo donde están las consultas (INSERT, DELETE, SELECT).
    // Room escribirá el código de esta función automáticamente por detrás.
    abstract fun artistDao(): ArtistDao

    // El bloque 'companion object' es como un "static" en Java.
    // Significa que podemos acceder a esto SIN tener que crear una instancia de la clase (Ej: AppDatabase.getDatabase())
    companion object {

        // @Volatile significa que esta variable será visible inmediatamente para todos los hilos (threads) del procesador.
        // Evita que un hilo vea un valor desactualizado de la base de datos.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Esta es la función principal que el resto de la app llama para pedir la base de datos
        fun getDatabase(context: Context): AppDatabase {

            // Operador Elvis (?:): Significa "Si INSTANCE no es nulo, devuélvelo. Si es nulo, ejecuta lo que hay a la derecha"
            return INSTANCE ?: synchronized(this) {
                // 'synchronized(this)' es un cerrojo de seguridad (hilo seguro / thread-safe).
                // Garantiza que si dos partes de la app piden la base de datos EXACTAMENTE a la vez,
                // solo una entre aquí, evitando que se creen dos bases de datos al mismo tiempo.

                // Construimos la base de datos real
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Usamos el contexto global de la app para evitar fugas de memoria
                    AppDatabase::class.java,    // La clase de la base de datos
                    "soundconnect_database"     // ¡El nombre del archivo físico que se guardará en el móvil!
                ).build()

                // Guardamos la base de datos recién creada en nuestra variable global
                INSTANCE = instance

                // Devolvemos la instancia para que quien la pidió la pueda usar
                instance
            }
        }
    }
}