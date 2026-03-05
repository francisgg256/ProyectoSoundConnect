package com.example.firebase.data.repository

// Importamos todas las herramientas necesarias de Local, Model y Network
import com.example.firebase.data.local.ArtistDao
import com.example.firebase.data.local.ArtistEntity
import com.example.firebase.data.model.Artist
import com.example.firebase.data.model.LastFmImage
import com.example.firebase.data.network.MusicApiService
import kotlinx.coroutines.flow.Flow

class MusicRepository(
    private val apiService: MusicApiService, // El traductor de internet (Retrofit)
    private val artistDao: ArtistDao         // El manual de la base de datos interna (Room)
) {

    // Función que busca artistas en Deezer y los "disfraza" para que encajen en tu app.
    suspend fun searchArtists(query: String): List<Artist> {
        return try {
            // Pide los datos a la API de internet.
            val response = apiService.searchArtists(query)

            // .map hace un "bucle". Coge cada artista que manda Deezer (DeezerArtist) y lo convierte en el modelo Artist de tu app.
            response.data.map { deezerArtist ->
                Artist(
                    name = deezerArtist.name,
                    listeners = deezerArtist.nb_fan.toString(), // Deezer lo llama nb_fan, tu app lo llama listeners.
                    image = listOf(LastFmImage(deezerArtist.picture_medium, "medium")) // Mete la foto en la lista.
                )
            }
        } catch (e: Exception) {
            emptyList() // Si no hay internet o falla, devuelve una lista vacía para que no se "rompa" la app.
        }
    }

    // Función que busca la URL del archivo mp3 para poder reproducirlo.
    suspend fun getArtistPreviewUrl(artistName: String): String? {
        return try {
            // Busca la canción principal del artista en la API.
            val response = apiService.searchTracks(artistName)

            // Coge la primera canción de la lista (firstOrNull) y devuelve su "preview" (el link del MP3 de 30 segundos).
            response.data.firstOrNull()?.preview
        } catch (e: Exception) {
            null // Si hay error, devuelve nulo para que no suene nada.
        }
    }

    // Funciones de la Base de Datos Local (Favoritos)
    // ----------------------------------------------

    // Pide a Room la lista de favoritos en modo "radio" (Flow) para que se actualice sola en pantalla.
    fun getAllFavorites(): Flow<List<ArtistEntity>> = artistDao.getAllFavorites()

    // Función inteligente que hace de interruptor (Toggle): Si tiene el corazón puesto se lo quita, si no lo tiene se lo pone.
    suspend fun toggleFavorite(artist: Artist) {
        val name = artist.name ?: return // Si el artista no tiene nombre, nos salimos.

        // Vamos a la memoria del móvil y miramos si ya está guardado.
        val existingFavorite = artistDao.getFavoriteByName(name)

        if (existingFavorite != null) {
            // Si ya estaba guardado (existingFavorite tiene algo), significa que el usuario quiere QUITARLO de favoritos.
            artistDao.deleteFavorite(existingFavorite)
        } else {
            // Si no estaba en la base de datos (es null), creamos el modelo Entity y lo GUARDAMOS.
            val entity = ArtistEntity(
                name = name,
                listeners = artist.listeners,
                imageUrl = artist.image?.lastOrNull()?.url
            )
            artistDao.insertFavorite(entity)
        }
    }
}