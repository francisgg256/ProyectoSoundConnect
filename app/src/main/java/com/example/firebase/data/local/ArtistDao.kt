package com.example.firebase.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    @Query("SELECT * FROM favorite_artists WHERE userId = :userId")
    fun getAllFavorites(userId: String): Flow<List<ArtistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(artist: ArtistEntity)

    @Delete
    suspend fun deleteFavorite(artist: ArtistEntity)

    @Query("SELECT * FROM favorite_artists WHERE name = :artistName AND userId = :userId LIMIT 1")
    suspend fun getFavoriteByName(artistName: String, userId: String): ArtistEntity?
}