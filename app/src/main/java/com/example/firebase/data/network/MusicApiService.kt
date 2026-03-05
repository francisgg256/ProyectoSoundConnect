package com.example.firebase.data.network

import com.example.firebase.data.model.DeezerResponse // NUEVO
import com.example.firebase.data.model.ItunesResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface MusicApiService {
    @GET("search/artist")
    suspend fun searchArtists(@Query("q") query: String): DeezerResponse

    @GET
    suspend fun getSongPreview(@Url url: String): ItunesResponse
}
