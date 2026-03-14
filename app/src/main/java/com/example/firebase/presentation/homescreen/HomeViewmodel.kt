package com.example.firebase.presentation.homescreen

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebase.data.local.ArtistEntity
import com.example.firebase.data.model.Artist
import com.example.firebase.data.model.Player
import com.example.firebase.data.repository.MusicRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
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

    private val _profileImage = MutableStateFlow<android.graphics.Bitmap?>(null)
    val profileImage: StateFlow<android.graphics.Bitmap?> = _profileImage

    private val _userName = MutableStateFlow("amante de la música")
    val userName: StateFlow<String> = _userName

    init {
        searchArtists("rock")
        getPlayer()
        reloadCurrentUser()

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            if (uid != null) {
                Log.i("SoundConnect", "Firebase despertó. Cargando base de datos del usuario: $uid")
                loadFavorites(uid)
            }
        }
    }

    fun reloadCurrentUser() {
        val user = Firebase.auth.currentUser
        _userName.value = user?.displayName?.takeIf { it.isNotBlank() } ?: "amante de la música"
    }

    private fun loadFavorites(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.getAllFavorites(uid).collect { favList ->
                Log.i("SoundConnect", "ÉXITO: La pantalla se ha actualizado con ${favList.size} favoritos.")
                _favorites.value = favList
            }
        }
    }

    fun onFavoriteClick(artist: Artist) {
        val currentUserId = Firebase.auth.currentUser?.uid 
        
        if (currentUserId == null) {
            Log.e("SoundConnect", "ERROR: No hay usuario al intentar guardar el favorito.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            Log.i("SoundConnect", "Guardando/Borrando de la base de datos a: ${artist.name}")
            musicRepository.toggleFavorite(artist, currentUserId)
        }
    }

    fun updateProfileImage(bitmap: android.graphics.Bitmap) {
        _profileImage.value = bitmap
    }

    fun updateUserName(newName: String) {
        if (newName.isBlank()) return

        val user = Firebase.auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = newName
        }

        user?.updateProfile(profileUpdates)?.addOnSuccessListener {
            _userName.value = newName
        }
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

    private fun collectPlayer(): Flow<DataSnapshot> = callbackFlow {
        val listener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
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
                .catch { e -> Log.e("SoundConnect", "Error: ${e.message}") }
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

    fun recommendRandomArtist() {
        val randomArtists = listOf(
            "Michael Jackson", "Queen", "Dua Lipa", "Eminem",
            "Bad Bunny", "Rosalía", "The Beatles", "Shakira",
            "AC/DC", "Rihanna", "Frank Sinatra"
        )
        val surpriseArtist = randomArtists.random()
        searchArtists(surpriseArtist)
    }

    fun clearSessionState() {
        _profileImage.value = null
        _favorites.value = emptyList()
        _player.value = null
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}