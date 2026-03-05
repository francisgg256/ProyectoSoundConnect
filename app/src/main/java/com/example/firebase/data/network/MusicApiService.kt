package com.example.firebase.data.network

import com.example.firebase.data.model.DeezerResponse
import com.example.firebase.data.model.DeezerTrackResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {

    // @GET("search/artist") le dice a la app: "Para esta función, conéctate a la URL base y añádele '/search/artist' al final".
    @GET("search/artist")
    // suspend: Significa que esta función puede tardar un poco (porque necesita internet) y debe ejecutarse en segundo plano para no congelar la pantalla.
    // @Query("q") query: String: Significa que si buscas "Eminem", Retrofit añadirá "?q=Eminem" a la URL web.
    // : DeezerResponse: Lo que nos devuelve internet se meterá automáticamente en el molde "DeezerResponse" que vimos antes.
    suspend fun searchArtists(@Query("q") query: String): DeezerResponse

    // Lo mismo que arriba, pero ahora va a la dirección "/search" para buscar canciones en lugar de artistas.
    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerTrackResponse
}
