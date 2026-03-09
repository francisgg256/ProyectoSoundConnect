package com.example.firebase.data.model

// Usamos 'data class' porque su objetivo exclusivo es empaquetar y transportar los datos de la chincheta.
data class MusicTag(
    // 'id' es el identificador único de la chincheta.
    // Cuando guardamos esto en Firebase, usamos la función 'push()', que genera un código alfanumérico único (ej: "-Nhja8934jk").
    val id: String = "",

    // 'artistName' guarda el nombre del artista que el usuario estaba escuchando
    // en el momento exacto en el que hizo clic en el mapa.
    val artistName: String = "",

    // 'lat' es la Latitud de la coordenada geográfica.
    // Usamos el tipo 'Double' (número decimal muy preciso) porque las coordenadas GPS de Google Maps
    // requieren muchos decimales para ser exactas (ej: 40.416775).
    val lat: Double = 0.0,

    // 'lng' es la Longitud de la coordenada geográfica.
    // Igual que la latitud, requiere formato 'Double'.
    val lng: Double = 0.0,

    // 'userName' es el nombre de la persona que dejó la chincheta.
    // Le ponemos "Anónimo" por defecto por si hay un error al leer el usuario, para que no devuelva un error 'null'.
    val userName: String = "Anónimo"
)