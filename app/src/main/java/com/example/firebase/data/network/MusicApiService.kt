package com.example.firebase.data.network

import com.example.firebase.data.model.MusicResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {
    @GET("2.0/?method=artist.search&format=json")
    suspend fun searchArtists(
        @Query("artist") artistName: String,
        @Query("api_key") apiKey: String
    ): MusicResponse
}
