package com.example.firebase.data.model

// Usamos 'data class' porque su función principal es agrupar la información de lo que está sonando ahora mismo.
data class Player(

    // 'artist' es un objeto complejo (la clase Artist que vimos antes).
    // Aquí guardamos tooooda la información del artista que está sonando (nombre, oyentes, fotos).
    // Le ponemos '? = null' para que, si no hay ninguna canción sonando, la variable simplemente se quede vacía sin romper la app.
    val artist: Artist? = null,

    // 'play' es un valor booleano (true o false).
    // Si es 'true', significa que la música está reproduciéndose (botón de pausa visible).
    // Si es 'false', significa que la música está en pausa (botón de play visible).
    // También tiene '? = null' por el mismo motivo que el resto de modelos de Firebase.
    val play: Boolean? = null
)
