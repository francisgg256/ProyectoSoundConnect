package com.example.firebase.data.local

import androidx.room.Entity

@Entity(tableName = "favorite_artists", primaryKeys = ["name", "userId"])
data class ArtistEntity(
    val name: String,
    val listeners: String?,
    val imageUrl: String?,
    val userId: String
)