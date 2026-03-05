package com.example.firebase.presentation.homescreen

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebase.data.local.ArtistEntity
import com.example.firebase.data.model.Artist
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

    private var database: FirebaseDatabase = Firebase.database
    private var mediaPlayer: MediaPlayer? = null

    private val _artist = MutableStateFlow<List<Artist>>(emptyList())
    val artist: StateFlow<List<Artist>> = _artist

    private val _player = MutableStateFlow<Player?>(null)
    val player: StateFlow<Player?> = _player

    private val _favorites = MutableStateFlow<List<ArtistEntity>>(emptyList())
    val favorites: StateFlow<List<ArtistEntity>> = _favorites

    private val _musicTags = MutableStateFlow<List<MusicTag>>(emptyList())
    val musicTags: StateFlow<List<MusicTag>> = _musicTags

    init {
        searchArtists("rock")
        getPlayer()
        getFavorites()
        getMusicTags()
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
                .collect {
                    val player = it.getValue(Player::class.java)
                    _player.value = player
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

    // --- MAPAS Y ETIQUETAS (Firebase) ---
    fun addMusicTag(artistName: String, latLng: LatLng) {
        // Creamos una nueva referencia en la carpeta "music_tags"
        val ref = database.reference.child("music_tags").push()

        val newTag = MusicTag(
            id = ref.key ?: "",
            artistName = artistName,
            lat = latLng.latitude,
            lng = latLng.longitude
        )
        // Guardamos en Firebase (esto actualiza a todos los usuarios)
        ref.setValue(newTag)
    }

    private fun getMusicTags() {
        val ref = database.reference.child("music_tags")
        // Escuchamos los cambios en tiempo real
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tagsList = mutableListOf<MusicTag>()
                for (child in snapshot.children) {
                    val tag = child.getValue(MusicTag::class.java)
                    if (tag != null) tagsList.add(tag)
                }
                _musicTags.value = tagsList // Actualizamos el mapa
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SoundConnect", "Error al leer chinchetas: ${error.message}")
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
