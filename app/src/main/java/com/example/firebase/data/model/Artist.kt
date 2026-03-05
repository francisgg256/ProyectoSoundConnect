package com.example.firebase.data.model

import com.google.gson.annotations.SerializedName

data class Artist(
    val name: String? = null,
    val listeners: String? = null,
    // ¡Aquí está la magia! Last.fm devuelve una lista de imágenes
    val image: List<LastFmImage>? = null 
)

data class LastFmImage(
    // Last.fm usa "#text" para la URL, usamos SerializedName para mapearlo
    @SerializedName("#text") val url: String = "", 
    val size: String = ""
)
