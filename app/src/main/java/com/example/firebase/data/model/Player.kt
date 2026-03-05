package com.example.firebase.data.model

data class Player(
    val artist: Artist? = null, // Guarda todos los datos del artista que está sonando en ese momento.
    val play: Boolean? = null // Si es true, la música está en "Play", si es false está en "Pause".
)
