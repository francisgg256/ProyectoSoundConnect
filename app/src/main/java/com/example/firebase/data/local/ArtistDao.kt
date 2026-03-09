package com.example.firebase.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// @Dao le dice a la librería Room: "Oye, este archivo contiene las instrucciones para leer y escribir en la base de datos".
// Room leerá esto y escribirá el código complejo de SQL por ti por debajo.
@Dao
interface ArtistDao {

    // @Query permite escribir comandos SQL puros.
    // Aquí le decimos: "Selecciona TODO (*) de la tabla 'favorite_artists'".
    // Devuelve un 'Flow<List<ArtistEntity>>'. El 'Flow' es mágico: significa que la lista se actualizará SOLA
    // en la pantalla cada vez que añadas o borres un favorito, sin tener que volver a llamar a la función.
    @Query("SELECT * FROM favorite_artists")
    fun getAllFavorites(): Flow<List<ArtistEntity>>

    // @Insert le dice a Room que esta función sirve para guardar un nuevo dato.
    // 'onConflict = OnConflictStrategy.REPLACE' significa: "Si intento guardar un artista que ya estaba guardado, reemplázalo por este nuevo en lugar de dar error".
    // La palabra 'suspend' significa que esta función es una Corrutina, es decir, se ejecutará en un hilo secundario
    // para no congelar la pantalla del móvil mientras guarda el dato.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(artist: ArtistEntity)

    // @Delete es autoexplicativo: le pasas un artista entero y Room lo busca en la tabla y lo borra.
    // También usa 'suspend' porque borrar datos lleva tiempo y debe hacerse en segundo plano.
    @Delete
    suspend fun deleteFavorite(artist: ArtistEntity)

    // Esta es una búsqueda específica. Le decimos: "Búscame en la tabla un artista cuyo nombre coincida con el nombre que te paso por parámetro (:artistName)".
    // 'LIMIT 1' asegura que, en el raro caso de que haya dos iguales, solo devuelva el primero y termine rápido.
    // Devuelve un 'ArtistEntity?' (con la interrogación al final). Esto significa que puede devolver el artista si lo encuentra, o 'null' si no existe en favoritos.
    @Query("SELECT * FROM favorite_artists WHERE name = :artistName LIMIT 1")
    suspend fun getFavoriteByName(artistName: String): ArtistEntity?
}