package com.example.firebase.data.repository

import com.example.firebase.data.local.ArtistDao
import com.example.firebase.data.local.ArtistEntity
import com.example.firebase.data.model.Artist
import com.example.firebase.data.model.LastFmImage
import com.example.firebase.data.network.MusicApiService
import kotlinx.coroutines.flow.Flow

class MusicRepository(
    private val apiService: MusicApiService,
    private val artistDao: ArtistDao
) {

    suspend fun searchArtists(query: String): List<Artist> {
        return try {
            val response = apiService.searchArtists(query)

            response.data.map { deezerArtist ->
                Artist(
                    name = deezerArtist.name,
                    listeners = deezerArtist.nb_fan.toString(),
                    image = listOf(LastFmImage(deezerArtist.picture_medium, "medium"))
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getArtistPreviewUrl(artistName: String): String? {
        return try {
            val response = apiService.searchTracks(artistName)

            response.data.firstOrNull()?.preview
        } catch (e: Exception) {
            null
        }
    }

    fun getAllFavorites(): Flow<List<ArtistEntity>> = artistDao.getAllFavorites()

    suspend fun toggleFavorite(artist: Artist) {
        val name = artist.name ?: return

        val existingFavorite = artistDao.getFavoriteByName(name)

        if (existingFavorite != null) {
            artistDao.deleteFavorite(existingFavorite)
        } else {
            val entity = ArtistEntity(
                name = name,
                listeners = artist.listeners,
                imageUrl = artist.image?.lastOrNull()?.url
            )
            artistDao.insertFavorite(entity)
        }
    }
}