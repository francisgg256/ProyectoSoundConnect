package com.example.firebase.data.repository

import com.example.firebase.data.local.ArtistDao
import com.example.firebase.data.local.ArtistEntity
import com.example.firebase.data.model.Artist
import com.example.firebase.data.network.MusicApiService
import kotlinx.coroutines.flow.Flow

class MusicRepository(
    private val apiService: MusicApiService,
    private val artistDao: ArtistDao
) {
    private val API_KEY = "0241b531cb32649c96aa26407e793da2"

    suspend fun searchArtists(query: String): List<Artist> {
        return try {
            val response = apiService.searchArtists(query, API_KEY)
            response.results.artistmatches.artist
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAllFavorites(): Flow<List<ArtistEntity>> = artistDao.getAllFavorites()

    suspend fun toggleFavorite(artist: Artist) {
        val name = artist.name ?: return

        val entity = ArtistEntity(
            name = name,
            listeners = artist.listeners,
            imageUrl = artist.image?.lastOrNull()?.url
        )

        if (artistDao.isFavorite(name)) {
            artistDao.deleteFavorite(entity)
        } else {
            artistDao.insertFavorite(entity)
        }
    }
}