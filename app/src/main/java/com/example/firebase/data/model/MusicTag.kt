package com.example.firebase.data.model

import com.google.android.gms.maps.model.LatLng

data class MusicTag(
    val artistName: String,
    val position: LatLng, // Coordenadas GPS (Latitud y Longitud)
    val userName: String = "Anonimo"
)