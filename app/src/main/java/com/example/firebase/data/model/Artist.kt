package com.example.firebase.data.model

import com.google.gson.annotations.SerializedName

data class Artist(
    val name: String? = null,
    val listeners: String? = null,
    val image: List<LastFmImage>? = null
)
data class LastFmImage(
    @SerializedName("#text") val url: String? = null,
    val size: String? = null
)
