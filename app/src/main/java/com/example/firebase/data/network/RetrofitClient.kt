package com.example.firebase.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// "object" en Kotlin es un patrón llamado Singleton.
// Significa que SOLO existirá UNA copia de este motor en toda la app para no gastar memoria a lo tonto creando cientos de conexiones.
object RetrofitClient {

    // La URL base principal de Deezer. Todas las peticiones del MusicApiService se pegarán detrás de esto.
    private const val BASE_URL = "https://api.deezer.com/"

    // "by lazy" significa que este motor NO se construirá al abrir la app,
    // sino que esperará pacientemente hasta la primera vez que busques una canción. (¡Ahorra batería y memoria!)
    val apiService: MusicApiService by lazy {
        Retrofit.Builder() // Empezamos a construir el motor.
            .baseUrl(BASE_URL) // Le damos la URL principal.

            // ¡Esto es magia pura! GSON es el traductor. Coge el texto incomprensible (JSON)
            // que devuelve la API de Deezer y lo convierte automáticamente en nuestros moldes de datos de la carpeta "model".
            .addConverterFactory(GsonConverterFactory.create())

            .build() // Construimos el motor final.
            .create(MusicApiService::class.java) // Le decimos: "Usa el diccionario de peticiones que hemos creado antes".
    }
}