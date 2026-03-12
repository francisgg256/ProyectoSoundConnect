package com.example.firebase.data.network

import com.example.firebase.data.model.DeezerResponse
import com.example.firebase.data.model.DeezerTrackResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {

    //busca el artista en deezer dependiendo del nombre que se escriba en el buscador
    @GET("search/artist")
    suspend fun searchArtists(@Query("q") query: String): DeezerResponse

    //lo mismo pero para canciones
    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerTrackResponse
}
