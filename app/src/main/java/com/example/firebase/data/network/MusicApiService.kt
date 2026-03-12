package com.example.firebase.data.network

import com.example.firebase.data.model.DeezerResponse
import com.example.firebase.data.model.DeezerTrackResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {

    @GET("search/artist")
    suspend fun searchArtists(@Query("q") query: String): DeezerResponse

    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerTrackResponse
}
