package com.example.firebase.data.model

data class DeezerResponse(
    val data: List<DeezerArtist> // Deezer nos devuelve una lista metida dentro de una variable llamada "data".
)

data class DeezerArtist(
    val name: String, // El nombre del artista.
    val nb_fan: Int, // Número de fans.
    val picture_medium: String // El link directo a su foto de tamaño medio.
)

data class DeezerTrackResponse(
    val data: List<DeezerTrack> // Deezer devuelve la lista de canciones dentro de "data".
)

data class DeezerTrack(
    val preview: String // Es el link directo al archivo .mp3 de 30 segundos de la canción.
)
