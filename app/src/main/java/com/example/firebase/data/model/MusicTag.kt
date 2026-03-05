package com.example.firebase.data.model

data class MusicTag(
    val id: String = "", // El DNI de la chincheta en la base de datos de Firebase.
    val artistName: String = "", // El nombre del artista que estaba sonando en ese momento.
    val lat: Double = 0.0, // La coordenada de Latitud.
    val lng: Double = 0.0, // La coordenada de Longitud.
    val userName: String = "Anónimo" // El nombre de la persona que puso la chincheta en el mapa.
)