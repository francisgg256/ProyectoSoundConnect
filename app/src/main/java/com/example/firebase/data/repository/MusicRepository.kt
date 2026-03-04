package com.example.firebase.data.repository

import com.example.firebase.data.model.Artist
import com.example.firebase.data.network.MusicApiService

class MusicRepository(private val apiService: MusicApiService) {
    private val API_KEY = "TU_API_KEY_AQUI" // Deberás obtener una en last.fm/api

    suspend fun searchArtists(query: String): List<Artist> {
        return try {
            val response = apiService.searchArtists(query, API_KEY)
            response.results.artistmatches.artist
        } catch (e: Exception) {
            emptyList()
        }
    }
}
