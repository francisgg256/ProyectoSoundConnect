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

    private var database: FirebaseDatabase = Firebase.database("https://soundconnect-3c760-default-rtdb.europe-west1.firebasedatabase.app")
    private var mediaPlayer: MediaPlayer? = null

    private val _artist = MutableStateFlow<List<Artist>>(emptyList())
    val artist: StateFlow<List<Artist>> = _artist

    private val _player = MutableStateFlow<Player?>(null)
    val player: StateFlow<Player?> = _player

    private val _favorites = MutableStateFlow<List<ArtistEntity>>(emptyList())
    val favorites: StateFlow<List<ArtistEntity>> = _favorites

    private val _musicTags = MutableStateFlow<List<MusicTag>>(emptyList())
    val musicTags: StateFlow<List<MusicTag>> = _musicTags

    private val _profileImage = MutableStateFlow<android.graphics.Bitmap?>(null)
    val profileImage: StateFlow<android.graphics.Bitmap?> = _profileImage

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    fun updateProfileImage(bitmap: android.graphics.Bitmap) {
        _profileImage.value = bitmap
    }

    init {
        searchArtists("rock")
        getPlayer()
        getFavorites()
        getMusicTags()
        getChatMessages()
    }

    fun searchArtists(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
            musicRepository.toggleFavorite(artist)
        }
    }

    private fun collectPlayer(): Flow<DataSnapshot> = callbackFlow {
        val listener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot).isSuccess
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
                        _player.value = player
                    } catch (e: Exception) {
                        Log.e("SoundConnect", "Fallo al leer Firebase: ${e.message}")
                    }
                }
        }
    }

    fun onPlaySelected() {
        if (player.value != null) {
            val willPlay = !(player.value?.play ?: false)
            val currentPlayer = _player.value?.copy(play = willPlay)
            val ref = database.reference.child("player")
            ref.setValue(currentPlayer)

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
        ref.setValue(null)

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun recommendRandomArtist() {
        val randomArtists = listOf(
            "Michael Jackson", "Queen", "Dua Lipa", "Eminem",
            "Bad Bunny", "Rosalía", "The Beatles", "Shakira",
            "AC/DC", "Rihanna", "Frank Sinatra"
        )
        val surpriseArtist = randomArtists.random()
        searchArtists(surpriseArtist)
    }

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

    fun addMusicTag(artistName: String, latLng: LatLng) {
        val ref = database.reference.child("music_tags").push()

        val newTag = MusicTag(
            id = ref.key ?: "",
            artistName = artistName,
            lat = latLng.latitude,
            lng = latLng.longitude
        )
        ref.setValue(newTag)
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
                _musicTags.value = tagsList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SoundConnect", "Error al leer chinchetas: ${error.message}")
            }
        })
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val ref = database.reference.child("chat_messages").push()
        val newMessage = ChatMessage(
            id = ref.key ?: "",
            userName = "Amante de la música",
            text = text,
            timestamp = System.currentTimeMillis()
        )
        
        ref.setValue(newMessage)
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
                _chatMessages.value = messagesList.sortedBy { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SoundConnect", "Error al leer el chat: ${error.message}")
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
