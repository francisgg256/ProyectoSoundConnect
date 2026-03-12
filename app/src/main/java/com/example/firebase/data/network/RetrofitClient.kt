package com.example.firebase.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    //url a la que nos vamos a conectar
    private const val BASE_URL = "https://api.deezer.com/"

    val apiService: MusicApiService by lazy {

        // construimos el cliente de Retrofit
        Retrofit.Builder()
            .baseUrl(BASE_URL) // le pasamos la URL principal de Deezer

            //retrofit convierte el texto de deezer en una de las data classes
            .addConverterFactory(GsonConverterFactory.create())

            // construimos el objeto final
            .build()

            //lo enlazamos con la interfaz
            .create(MusicApiService::class.java)
    }
}