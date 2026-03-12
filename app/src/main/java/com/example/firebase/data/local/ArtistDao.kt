package com.example.firebase.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    //hace un select de toda la tabla de favorite_artists
    @Query("SELECT * FROM favorite_artists")
    fun getAllFavorites(): Flow<List<ArtistEntity>>

    // hacemos un insert de un nuevo artista en favoritos reemplazandolo si ya estaba guardado
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(artist: ArtistEntity)

    //borra un artista de favoritos
    @Delete
    suspend fun deleteFavorite(artist: ArtistEntity)

    //busca el artista en favoritos con el mismo nombre del que se le pasa por parametro
    @Query("SELECT * FROM favorite_artists WHERE name = :artistName LIMIT 1")
    suspend fun getFavoriteByName(artistName: String): ArtistEntity?
}