package com.example.firebase.data.model

import com.google.gson.annotations.SerializedName

data class Artist(
    val name: String? = null, //guardamos el nombre del artista
    val listeners: String? = null,//guardamos la cantidad de oyentes
    val image: List<LastFmImage>? = null//guardamos una lista con las imagenes
)
data class LastFmImage(
    // uso la API de Last.fm para poder usar las fotos con la URL que manda dentro de una etiqueta llamada "#text".
    // para poder usar esa variable "#text" usamos @SerializedName para poder guardarla en la variable URL
    @SerializedName("#text") val url: String? = null,
    val size: String? = null//guarda el tamaño de la foto
)
