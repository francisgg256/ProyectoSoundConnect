package com.example.firebase.data.model

data class MusicTag(
    //id de la chincheta
    val id: String = "",

    //nombre de la artista
    val artistName: String = "",

    // latitud de la coordenada geográfica
    val lat: Double = 0.0,

    //longitud de la coordenada geográfica
    val lng: Double = 0.0,

    //nombre del usuario que ha puesto la chincheta
    val userName: String = "Anónimo"
)