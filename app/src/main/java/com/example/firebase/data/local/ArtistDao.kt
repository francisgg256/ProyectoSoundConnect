package com.example.firebase.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// @Dao avisa a Android de que aquí están las funciones para tocar la base de datos.
@Dao
interface ArtistDao {

    // Función para leer TODOS los favoritos.
    // Al usar "Flow", la app se queda "escuchando". Si añades un favorito nuevo, la pantalla se actualiza sola al instante sin que hagas nada.
    @Query("SELECT * FROM favorite_artists")
    fun getAllFavorites(): Flow<List<ArtistEntity>>

    // Función para GUARDAR un favorito.
    // suspend: Porque tocar el disco duro lleva su tiempo y no queremos congelar la app.
    // onConflict = OnConflictStrategy.REPLACE: Significa "Si el artista ya estaba en favoritos y lo intentas meter otra vez, simplemente machaca el viejo y pon el nuevo".
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(artist: ArtistEntity)

    // Función para BORRAR un favorito.
    @Delete
    suspend fun deleteFavorite(artist: ArtistEntity)

    // Función para BUSCAR un solo artista por su nombre.
    // "LIMIT 1" asegura que solo nos devuelva uno (o ninguno si no es favorito).
    @Query("SELECT * FROM favorite_artists WHERE name = :artistName LIMIT 1")
    suspend fun getFavoriteByName(artistName: String): ArtistEntity?
}