package com.example.firebase.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity le dice a Room: "Crea una tabla en la memoria del móvil y llámala 'favorite_artists'".
@Entity(tableName = "favorite_artists")
data class ArtistEntity(
    // @PrimaryKey significa "Clave Principal".
    // Es como el DNI, no puede haber dos artistas favoritos con exactamente el mismo nombre.
    @PrimaryKey val name: String,

    // Las columnas normales de la tabla:
    val listeners: String?, // Guarda los oyentes
    val imageUrl: String?   // Guarda la URL de su foto
)