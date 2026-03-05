package com.example.firebase.data.model

data class DeezerResponse(
    val data: List<DeezerArtist>
)

data class DeezerArtist(
    val name: String,
    val nb_fan: Int,
    val picture_medium: String
)
