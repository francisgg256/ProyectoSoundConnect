package com.example.firebase.data.model

data class DeezerResponse(
    val data: List<DeezerArtist> // lista de artistas
)

data class DeezerArtist(
    val name: String, // nombre del artista

    //numero de oyentes
    val nb_fan: Int,

    //url de la foto
    val picture_medium: String
)

data class DeezerTrackResponse(
    val data: List<DeezerTrack> // lista de canciones
)

data class DeezerTrack(
    // preview de 30 segundos de la canción
    val preview: String
)
