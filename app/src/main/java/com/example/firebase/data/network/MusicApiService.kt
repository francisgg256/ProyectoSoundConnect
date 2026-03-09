package com.example.firebase.data.network

import com.example.firebase.data.model.DeezerResponse
import com.example.firebase.data.model.DeezerTrackResponse
import retrofit2.http.GET
import retrofit2.http.Query

// Es una 'interface' porque nosotros no escribimos el código real que hace la conexión a internet.
// Retrofit leerá esta interfaz y generará todo el código complejo de red por nosotros automáticamente.
interface MusicApiService {

    // --- BÚSQUEDA DE ARTISTAS ---
    // @GET indica el tipo de petición HTTP.
    // "search/artist" es el "endpoint" (la parte final de la URL).
    // Si la URL base es "https://api.deezer.com/", esto llamará a "https://api.deezer.com/search/artist"
    @GET("search/artist")

    // 'suspend' indica que esta función es una Corrutina (se ejecutará en segundo plano para no congelar la app).
    // @Query("q") hace magia: coge el texto que le pasas en la variable 'query' (ej: "Queen")
    // y lo añade a la URL así: "https://api.deezer.com/search/artist?q=Queen".
    // Finalmente, espera recibir un objeto de tipo 'DeezerResponse' (el modelo que creamos antes).
    suspend fun searchArtists(@Query("q") query: String): DeezerResponse

    // --- BÚSQUEDA DE CANCIONES ---
    // En Deezer, el endpoint "search" a secas sirve para buscar canciones o pistas (tracks).
    @GET("search")

    // Igual que arriba: es una corrutina, mete la palabra a buscar en el parámetro "?q=" de la URL,
    // pero esta vez espera recibir un 'DeezerTrackResponse' (el modelo que contiene el 'preview' mp3).
    suspend fun searchTracks(@Query("q") query: String): DeezerTrackResponse
}
