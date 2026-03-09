package com.example.firebase.data.repository

import com.example.firebase.data.local.ArtistDao
import com.example.firebase.data.local.ArtistEntity
import com.example.firebase.data.model.Artist
import com.example.firebase.data.model.LastFmImage
import com.example.firebase.data.network.MusicApiService
import kotlinx.coroutines.flow.Flow

// La clase recibe por parámetro la conexión a internet (apiService) y la base de datos local (artistDao).
// A esto se le llama "Inyección de Dependencias".
class MusicRepository(
    private val apiService: MusicApiService,
    private val artistDao: ArtistDao
) {

    // --- FUNCIONES DE INTERNET (DEEZER API) ---

    // Función asíncrona (suspend) para buscar artistas. Devuelve una lista de nuestro modelo "Artist".
    suspend fun searchArtists(query: String): List<Artist> {
        return try {
            // 1. Pedimos los datos brutos a Deezer
            val response = apiService.searchArtists(query)

            // 2. Mapeo (Transformación): Cogemos la lista que nos dio Deezer (DeezerArtist)
            // y la convertimos una a una (.map) en nuestro propio modelo de datos (Artist).
            response.data.map { deezerArtist ->
                Artist(
                    name = deezerArtist.name,
                    listeners = deezerArtist.nb_fan.toString(), // Convertimos el Int a String
                    // Deezer da la foto suelta, nuestro modelo pide una lista, así que la metemos en un listOf()
                    image = listOf(LastFmImage(deezerArtist.picture_medium, "medium"))
                )
            }
        } catch (e: Exception) {
            // Si no hay internet o falla la API, devolvemos una lista vacía para que no explote la app.
            emptyList()
        }
    }

    // Busca las canciones de un artista y devuelve SOLO la URL del audio mp3.
    suspend fun getArtistPreviewUrl(artistName: String): String? {
        return try {
            val response = apiService.searchTracks(artistName)
            // Coge el primer resultado de la lista (firstOrNull). Si existe, devuelve su 'preview' (la URL).
            response.data.firstOrNull()?.preview
        } catch (e: Exception) {
            null // Si hay error, devuelve nulo
        }
    }

    // --- FUNCIONES DE BASE DE DATOS LOCAL (ROOM) ---

    // Le pide a Room la lista de favoritos.
    // Como devuelve un 'Flow' (flujo continuo), no hace falta ponerle 'suspend'.
    fun getAllFavorites(): Flow<List<ArtistEntity>> = artistDao.getAllFavorites()

    // Función de tipo "Interruptor" (Toggle) para añadir o quitar favoritos.
    suspend fun toggleFavorite(artist: Artist) {
        // Si el artista no tiene nombre, nos salimos de la función (return) por seguridad.
        val name = artist.name ?: return

        // Miramos en Room a ver si ya lo teníamos guardado
        val existingFavorite = artistDao.getFavoriteByName(name)

        if (existingFavorite != null) {
            // Si ya existía, significa que el usuario ha vuelto a darle al corazón para QUITARLO.
            artistDao.deleteFavorite(existingFavorite)
        } else {
            // Si es 'null', significa que no estaba. Lo transformamos a Entidad (Entity) y lo GUARDAMOS.
            val entity = ArtistEntity(
                name = name,
                listeners = artist.listeners,
                imageUrl = artist.image?.lastOrNull()?.url
            )
            artistDao.insertFavorite(entity)
        }
    }
}