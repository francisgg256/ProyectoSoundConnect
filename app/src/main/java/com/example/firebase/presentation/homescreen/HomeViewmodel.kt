package com.example.firebase.presentation.homescreen

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebase.data.local.ArtistEntity
import com.example.firebase.data.model.Artist
import com.example.firebase.data.model.ChatMessage
import com.example.firebase.data.model.MusicTag
import com.example.firebase.data.model.Player
import com.example.firebase.data.repository.MusicRepository
import com.google.android.gms.maps.model.LatLng
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

class HomeViewmodel(private val musicRepository: MusicRepository) : ViewModel() {

    // 1. CONEXIONES: Base de datos en Europa y Reproductor de Audio
    private var database: FirebaseDatabase = Firebase.database("https://soundconnect-3c760-default-rtdb.europe-west1.firebasedatabase.app")
    private var mediaPlayer: MediaPlayer? = null

    // 2. ESTADOS: Variables que la pantalla "escucha" para redibujarse
    private val _artist = MutableStateFlow<List<Artist>>(emptyList())
    val artist: StateFlow<List<Artist>> = _artist

    private val _player = MutableStateFlow<Player?>(null)
    val player: StateFlow<Player?> = _player // Controla la barra morada inferior

    private val _favorites = MutableStateFlow<List<ArtistEntity>>(emptyList())
    val favorites: StateFlow<List<ArtistEntity>> = _favorites // Lista de corazones rojos

    private val _musicTags = MutableStateFlow<List<MusicTag>>(emptyList())
    val musicTags: StateFlow<List<MusicTag>> = _musicTags // Chinchetas del mapa

    private val _profileImage = MutableStateFlow<android.graphics.Bitmap?>(null)
    val profileImage: StateFlow<android.graphics.Bitmap?> = _profileImage // Foto del usuario

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages // Mensajes del chat

    // Función para guardar la foto que devuelve la cámara
    fun updateProfileImage(bitmap: android.graphics.Bitmap) {
        _profileImage.value = bitmap
    }

    // El bloque init se ejecuta automáticamente al abrir la app
    init {
        searchArtists("rock") // Búsqueda por defecto
        getPlayer()           // Escucha si hay música sonando en la nube
        getFavorites()        // Carga los favoritos de la memoria del móvil
        getMusicTags()        // Carga las chinchetas de Firebase
        getChatMessages()     // Carga el chat de Firebase
    }

    // --- MÚSICA Y FAVORITOS ---
    fun searchArtists(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Pide datos a Deezer a través del Repositorio
                val results = musicRepository.searchArtists(query)
                _artist.value = results
            } catch (e: Exception) {
                Log.e("SoundConnect", "Error buscando en API: ${e.message}")
            }
        }
    }

    private fun getFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.getAllFavorites().collect { favList ->
                _favorites.value = favList
            }
        }
    }

    fun onFavoriteClick(artist: Artist) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.toggleFavorite(artist) // Añade o quita el favorito
        }
    }

    // --- REPRODUCTOR SINCRONIZADO EN FIREBASE ---
    private fun collectPlayer(): Flow<DataSnapshot> = callbackFlow {
        val listener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot).isSuccess // Avisa si hay cambios en Firebase
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("SoundConnect","Firebase Error: ${error.message}")
                close(error.toException())
            }
        }
        val ref = database.reference.child("player")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private fun getPlayer() {
        viewModelScope.launch {
            collectPlayer()
                .catch { e ->
                    Log.e("SoundConnect", "Error collecting player: ${e.message}")
                }
                .collect { snapshot ->
                    try {
                        val player = snapshot.getValue(Player::class.java)
                        _player.value = player // Muestra la barra morada si hay datos
                    } catch (e: Exception) {
                        Log.e("SoundConnect", "Fallo al leer Firebase: ${e.message}")
                    }
                }
        }
    }

    fun onPlaySelected() {
        if (player.value != null) {
            val willPlay = !(player.value?.play ?: false) // Alterna entre Play y Pause
            val currentPlayer = _player.value?.copy(play = willPlay)

            // Avisa a la nube del cambio
            val ref = database.reference.child("player")
            ref.setValue(currentPlayer)

            // Descarga el audio mp3 y lo reproduce si le dimos a Play
            if (willPlay) {
                val artistName = currentPlayer?.artist?.name ?: return
                viewModelScope.launch(Dispatchers.IO) {
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
            } else {
                mediaPlayer?.pause()
            }
        }
    }

    fun onCancelSelected() {
        val ref = database.reference.child("player")
        ref.setValue(null) // Borra la carpeta player de Firebase

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Se llama al agitar el móvil (Acelerómetro)
    fun recommendRandomArtist() {
        val randomArtists = listOf(
            "Michael Jackson", "Queen", "Dua Lipa", "Eminem",
            "Bad Bunny", "Rosalía", "The Beatles", "Shakira",
            "AC/DC", "Rihanna", "Frank Sinatra"
        )
        val surpriseArtist = randomArtists.random()
        searchArtists(surpriseArtist)
    }

    // Se ejecuta al hacer clic en un artista de la lista
    fun addPlayer(artist: Artist) {
        val ref = database.reference.child("player")
        val player = Player(artist, play = true)

        ref.setValue(player)
            .addOnSuccessListener {
                Log.i("SoundConnect", "¡Éxito! Canción guardada en Firebase Europa.")
            }
            .addOnFailureListener { error ->
                Log.e("SoundConnect", "Fallo al guardar en Firebase: ${error.message}")
            }

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

    // --- MAPAS ---
    fun addMusicTag(artistName: String, latLng: LatLng) {
        val ref = database.reference.child("music_tags").push()

        val newTag = MusicTag(
            id = ref.key ?: "",
            artistName = artistName,
            lat = latLng.latitude,
            lng = latLng.longitude
        )
        ref.setValue(newTag) // Sube la chincheta a Firebase
    }

    private fun getMusicTags() {
        val ref = database.reference.child("music_tags")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tagsList = mutableListOf<MusicTag>()
                for (child in snapshot.children) {
                    val tag = child.getValue(MusicTag::class.java)
                    if (tag != null) tagsList.add(tag)
                }
                _musicTags.value = tagsList // Dibuja las chinchetas
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SoundConnect", "Error al leer chinchetas: ${error.message}")
            }
        })
    }

    // --- CHAT GLOBAL ---
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val ref = database.reference.child("chat_messages").push()
        val newMessage = ChatMessage(
            id = ref.key ?: "",
            userName = "Amante de la música",
            text = text,
            timestamp = System.currentTimeMillis()
        )

        ref.setValue(newMessage) // Sube el mensaje a la nube
            .addOnSuccessListener { Log.i("SoundConnect", "Mensaje enviado al chat") }
            .addOnFailureListener { Log.e("SoundConnect", "Error al enviar mensaje: ${it.message}") }
    }

    private fun getChatMessages() {
        val ref = database.reference.child("chat_messages")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    val message = child.getValue(ChatMessage::class.java)
                    if (message != null) {
                        messagesList.add(message)
                    }
                }
                _chatMessages.value = messagesList.sortedBy { it.timestamp } // Ordena por fecha
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SoundConnect", "Error al leer el chat: ${error.message}")
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release() // Limpia la memoria al cerrar la app
        mediaPlayer = null
    }
}
