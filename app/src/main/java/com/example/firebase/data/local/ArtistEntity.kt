package com.example.firebase.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_artists")
data class ArtistEntity(
    @PrimaryKey val name: String,
    val listeners: String?,
    val imageUrl: String?
)