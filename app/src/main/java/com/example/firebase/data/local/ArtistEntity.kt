package com.example.firebase.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_artists")
data class ArtistEntity(

    //usamos el nombre como llave privada para evitar duplicados
    @PrimaryKey val name: String,

    //guardamos el numero de oyentes
    val listeners: String?,

    //guardamos la foto del artista
    val imageUrl: String?
)