package com.example.firebase.presentation.homescreen

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebase.data.local.ArtistEntity
import com.example.firebase.data.model.Artist
import com.example.firebase.data.model.Player
import com.example.firebase.data.repository.MusicRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// Hereda de ViewModel e inyecta el MusicRepository (Clean Architecture)
class HomeViewmodel(private val musicRepository: MusicRepository) : ViewModel() {

    // 1. CONEXIÓN A FIREBASE Y REPRODUCTOR NATIVO
    // Pasamos la URL exacta de Europa para que Firebase Realtime Database no se pierda.
    private var database: FirebaseDatabase = Firebase.database("https://soundconnect-3c760-default-rtdb.europe-west1.firebasedatabase.app")

    // Objeto nativo de Android que sirve para reproducir audio o vídeo. Lo inicializamos a null.
    private var mediaPlayer: MediaPlayer? = null

    // 2. ESTADOS DE LA PANTALLA (STATEFLOWS)
    // El patrón es siempre el mismo: Una variable privada mutable (_) y una pública inmutable.

    // Lista de artistas que vienen de internet (Deezer)
    private val _artist = MutableStateFlow<List<Artist>>(emptyList())
    val artist: StateFlow<List<Artist>> = _artist

    // El estado del reproductor sincronizado en la nube (Qué suena y si está en Play/Pause)
    private val _player = MutableStateFlow<Player?>(null)
    val player: StateFlow<Player?> = _player

    // Lista de favoritos que vienen de SQLite (Room)
    private val _favorites = MutableStateFlow<List<ArtistEntity>>(emptyList())
    val favorites: StateFlow<List<ArtistEntity>> = _favorites

    // La foto de perfil que hemos capturado con la cámara (Bitmap)
    private val _profileImage = MutableStateFlow<android.graphics.Bitmap?>(null)
    val profileImage: StateFlow<android.graphics.Bitmap?> = _profileImage

    // Función sencilla para guardar la foto de la cámara en el StateFlow
    fun updateProfileImage(bitmap: android.graphics.Bitmap) {
        _profileImage.value = bitmap
    }

    // 3. INICIALIZACIÓN (Lo que arranca al abrir la pantalla)
    init {
        searchArtists("rock") // Hace una búsqueda inicial por defecto para que la pantalla no salga vacía
        getPlayer() // Empieza a escuchar si hay alguien reproduciendo música en Firebase
        getFavorites() // Carga los corazones rojos guardados
    }

    // --- FUNCIONES DE BASE DE DATOS Y API ---

    // Busca en Deezer
    fun searchArtists(query: String) {
        if (query.isBlank()) return

        // Abrimos un hilo secundario de entrada/salida (Dispatchers.IO) para no congelar la pantalla
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // El repositorio hace la llamada y transforma los datos
                val results = musicRepository.searchArtists(query)
                _artist.value = results // Actualizamos la pantalla con los resultados
            } catch (e: Exception) {
                Log.e("SoundConnect", "Error buscando en API: ${e.message}")
            }
        }
    }

    // Lee los favoritos de Room
    private fun getFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            // Como el DAO de Room ya devuelve un Flow, solo tenemos que hacer .collect{}
            musicRepository.getAllFavorites().collect { favList ->
                _favorites.value = favList // Actualizamos el estado
            }
        }
    }

    // Añade o quita un favorito (Interruptor/Toggle)
    fun onFavoriteClick(artist: Artist) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.toggleFavorite(artist)
        }
    }

    // --- MAGIA AVANZADA: FIREBASE + CORRUTINAS ---

    // Firebase usa un sistema antiguo llamado "Callbacks" (ValueEventListener).
    // Nosotros usamos `callbackFlow` para "envolver" a Firebase y transformarlo en un moderno Flujo de Corrutinas.
    private fun collectPlayer(): Flow<DataSnapshot> = callbackFlow {
        val listener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Cuando Firebase detecta un cambio, lo "escupe" hacia el Flow
                trySend(snapshot).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Si hay error, cierra el flujo
            }
        }
        val ref = database.reference.child("player")
        ref.addValueEventListener(listener) // Empezamos a escuchar a la nube

        // ¡SUPER IMPORTANTE! Cuando el flujo se destruye, borramos el listener de Firebase para no consumir batería.
        awaitClose { ref.removeEventListener(listener) }
    }

    // Esta función llama al callbackFlow de arriba y guarda el resultado en el _player de la pantalla
    private fun getPlayer() {
        viewModelScope.launch {
            collectPlayer()
                .catch { e -> Log.e("SoundConnect", "Error: ${e.message}") }
                .collect { snapshot ->
                    try {
                        // Convierte el JSON loco de Firebase a nuestro modelo 'Player'
                        val player = snapshot.getValue(Player::class.java)
                        _player.value = player // ¡Pum! La barra de reproducción aparece en pantalla
                    } catch (e: Exception) {
                        Log.e("SoundConnect", "Fallo al leer Firebase: ${e.message}")
                    }
                }
        }
    }

    // --- CONTROLES DEL REPRODUCTOR (PLAY/PAUSE/CANCEL) ---

    // Cuando el usuario le da al botón Play o Pausa en la barrita morada...
    fun onPlaySelected() {
        if (player.value != null) {
            // Invertimos el valor. Si estaba sonando (true), ahora es pausa (false).
            val willPlay = !(player.value?.play ?: false)
            val currentPlayer = _player.value?.copy(play = willPlay)

            // 1. Avisamos a Firebase del cambio para que se actualice en el móvil de todos los usuarios
            val ref = database.reference.child("player")
            ref.setValue(currentPlayer)

            // 2. Controlamos nuestro propio audio
            if (willPlay) {
                val artistName = currentPlayer?.artist?.name ?: return
                viewModelScope.launch(Dispatchers.IO) {
                    // Buscamos el enlace mp3 en Deezer
                    val audioUrl = musicRepository.getArtistPreviewUrl(artistName)
                    if (audioUrl != null) {
                        mediaPlayer?.release() // Matamos cualquier canción vieja que estuviera sonando
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(audioUrl) // Le pasamos el enlace mp3
                            prepareAsync() // Lo prepara en segundo plano (internet)
                            setOnPreparedListener { start() } // Cuando termine de cargar, que suene
                        }
                    }
                }
            } else {
                mediaPlayer?.pause() // Si hemos pulsado pausa, pausamos el audio local
            }
        }
    }

    // Cuando pulsamos la X de cerrar la barrita de música
    fun onCancelSelected() {
        // Borramos el nodo de Firebase mandando un 'null'. Esto hará que la barrita desaparezca para todos.
        val ref = database.reference.child("player")
        ref.setValue(null)

        // Paramos el audio y LIBERAMOS LA MEMORIA DEL MÓVIL
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // --- EVENTO DEL SENSOR ACELERÓMETRO (Agitar) ---
    // Esta función es llamada desde el ShakeDetector (MainActivity) cuando agitas el teléfono.
    fun recommendRandomArtist() {
        val randomArtists = listOf(
            "Michael Jackson", "Queen", "Dua Lipa", "Eminem",
            "Bad Bunny", "Rosalía", "The Beatles", "Shakira",
            "AC/DC", "Rihanna", "Frank Sinatra"
        )
        val surpriseArtist = randomArtists.random() // Elige uno al azar
        searchArtists(surpriseArtist) // Automáticamente lo busca en internet
    }

    // --- SELECCIONAR CANCIÓN NUEVA DE LA LISTA ---
    // Cuando el usuario pincha en un artista del buscador...
    fun addPlayer(artist: Artist) {
        val ref = database.reference.child("player")
        val player = Player(artist, play = true)

        // 1. Lo sube a Firebase avisando de que alguien ha puesto una canción nueva
        ref.setValue(player)

        // 2. Hace la llamada a Deezer para sacar el mp3 y lo reproduce
        viewModelScope.launch(Dispatchers.IO) {
            val artistName = artist.name ?: return@launch
            val audioUrl = musicRepository.getArtistPreviewUrl(artistName)

            if (audioUrl != null) {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioUrl)
                    prepareAsync()
                    setOnPreparedListener { start() }
                }
            }
        }
    }

    // --- CICLO DE VIDA (MUY IMPORTANTE) ---
    // onCleared se ejecuta cuando Android decide cerrar/destruir este ViewModel (ej. cuando cerramos la app del todo).
    override fun onCleared() {
        super.onCleared()
        // Liberamos el MediaPlayer para que la música no se quede sonando como un fantasma en el móvil.
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
