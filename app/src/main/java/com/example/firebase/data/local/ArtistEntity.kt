package com.example.firebase.data.local

// Importamos las etiquetas (anotaciones) de la librería Room
import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity le dice a Room: "Esta clase no es una clase normal, es una TABLA de base de datos".
// 'tableName = "favorite_artists"' le da un nombre específico a esa tabla en SQLite.
// Si no pusiéramos 'tableName', la tabla se llamaría "ArtistEntity" por defecto.
@Entity(tableName = "favorite_artists")
data class ArtistEntity( // Usamos 'data class' en Kotlin porque esta clase solo sirve para transportar y guardar datos, no tiene lógica compleja.

    // @PrimaryKey significa "Clave Primaria". Es el identificador ÚNICO de cada fila en la tabla.
    // En este caso, usamos el propio nombre del artista como ID único.
    // Así, si intentamos guardar a "Queen" dos veces, la base de datos sabrá que es el mismo y no lo duplicará.
    @PrimaryKey val name: String,

    // Esta columna guardará el texto con la cantidad de oyentes.
    // El símbolo '?' al final del String significa que es "Nullable" (puede ser nulo).
    // Lo ponemos así por si alguna vez la API de Deezer no nos devuelve los oyentes, la app no se crashee al intentar guardarlo.
    val listeners: String?,

    // Esta columna guardará el enlace (URL) de la foto del artista de internet.
    // También tiene '?' por si algún artista no tiene foto asignada.
    val imageUrl: String?
)