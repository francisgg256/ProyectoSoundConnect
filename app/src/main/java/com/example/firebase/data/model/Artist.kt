package com.example.firebase.data.model

// Importamos la herramienta de Gson que nos permite "traducir" claves raras de JSON
import com.google.gson.annotations.SerializedName

// Usamos 'data class' porque su único propósito es almacenar datos (no tienen funciones lógicas)
// Le ponemos '= null' a todas las variables. Esto es un "salvavidas":
// si la API falla y no nos envía un dato, Kotlin le pondrá 'null' en lugar de cerrar la app de golpe.
data class Artist(
    val name: String? = null,      // El nombre del artista
    val listeners: String? = null, // Número de oyentes (o fans)
    val image: List<LastFmImage>? = null // Una lista con las diferentes fotos del artista (pequeña, mediana, grande)
)

data class LastFmImage(
    // @SerializedName es magia pura de Retrofit/Gson.
    // La API (originalmente Last.fm por el nombre) devuelve un JSON donde la URL de la foto viene en una etiqueta llamada "#text".
    // Como en Kotlin no podemos crear una variable que empiece por "#" (daría error de sintaxis),
    // usamos @SerializedName("#text") para decirle a Gson: "Oye, lo que venga en '#text', guárdalo en mi variable llamada 'url'".
    @SerializedName("#text") val url: String? = null,

    // El tamaño de la foto (ej: "small", "medium", "large")
    val size: String? = null
)
