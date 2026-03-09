package com.example.firebase.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Usamos la palabra reservada 'object' en lugar de 'class'.
// En Kotlin, 'object' crea automáticamente un Singleton (una única instancia en toda la app).
// No queremos crear 20 conexiones a internet distintas, solo necesitamos una que todos compartan.
object RetrofitClient {

    // Definimos la URL base a la que nos vamos a conectar.
    // 'const val' significa que es una constante en tiempo de compilación (no puede cambiar nunca).
    private const val BASE_URL = "https://api.deezer.com/"

    // Creamos la variable que el resto de la app usará para pedir canciones.
    // Usamos 'by lazy' (inicialización perezosa). Esto es un truco de rendimiento:
    // Significa que todo el bloque de código de abajo NO se ejecutará hasta que el usuario intente buscar una canción por primera vez.
    val apiService: MusicApiService by lazy {

        // Construimos el cliente de Retrofit
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Le pasamos la URL principal de Deezer

            // Aquí ocurre la magia: Le decimos a Retrofit que cuando Deezer nos mande un texto en formato JSON,
            // use la librería Gson para convertirlo automáticamente en nuestras Data Classes (Artist, DeezerResponse, etc.)
            .addConverterFactory(GsonConverterFactory.create())

            // Construimos el objeto final
            .build()

            // Lo enlazamos con nuestra interfaz, para que Retrofit implemente las funciones '@GET' que escribimos antes
            .create(MusicApiService::class.java)
    }
}