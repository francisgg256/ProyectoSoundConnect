package com.example.firebase.data.model

import com.google.gson.annotations.SerializedName

data class Artist(
    val name: String? = null,
    val listeners: String? = null,
    val image: List<LastFmImage>? = null
)

// Asegúrate de que esta clase también tenga valores por defecto
data class LastFmImage(
    @SerializedName("#text") val url: String? = null,
    val size: String? = null
)
