package com.example.firebase.data.model

// --- MODELOS PARA BUSCAR ARTISTAS ---

// Cuando buscamos un artista en Deezer, el JSON principal nos devuelve una lista dentro de una clave llamada "data".
// Esta clase representa ese envoltorio principal.
data class DeezerResponse(
    val data: List<DeezerArtist> // Una lista de los artistas que ha encontrado
)

// Este es el modelo de un artista individual tal y como lo devuelve Deezer.
data class DeezerArtist(
    val name: String,           // El nombre del artista (ej: "Eminem")

    // En Deezer, el número de oyentes se llama "nb_fan" (Number of Fans).
    // Mantenemos este nombre EXACTO para que Gson sepa dónde encajar el dato del JSON.
    val nb_fan: Int,

    // URL de la foto del artista en tamaño mediano.
    // Deezer envía muchos tamaños (picture_small, picture_xl), pero nosotros filtramos solo la mediana.
    val picture_medium: String
)

// --- MODELOS PARA BUSCAR CANCIONES (EL AUDIO) ---

// Cuando le pedimos a Deezer las canciones de un artista, también las envuelve en una clave "data".
data class DeezerTrackResponse(
    val data: List<DeezerTrack> // Una lista de las canciones (pistas) encontradas
)

// Este es el modelo de una canción individual.
data class DeezerTrack(
    // De tooooodos los datos de la canción (título, duración, álbum...),
    // ¡A nosotros solo nos interesa el 'preview'!
    // Es un enlace (URL) directo a un archivo .mp3 de 30 segundos listo para el MediaPlayer.
    val preview: String
)
