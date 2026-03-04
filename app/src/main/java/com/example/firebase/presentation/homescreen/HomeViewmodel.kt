package com.example.firebase.presentation.homescreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebase.data.model.Artist
import com.example.firebase.data.model.Player
import com.example.firebase.data.repository.MusicRepository // Importamos tu repositorio
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

// Pasamos el repositorio por el constructor (Clean Architecture)
class HomeViewmodel(private val musicRepository: MusicRepository) : ViewModel() {

    // Ya no usamos FirebaseFirestore aquí.

    private var database: FirebaseDatabase = Firebase.database

    private val _artist = MutableStateFlow<List<Artist>>(emptyList())
    val artist: StateFlow<List<Artist>> = _artist

    private val _player = MutableStateFlow<Player?>(null)
    val player: StateFlow<Player?> = _player

    init {
        // Al iniciar, buscamos algunos artistas por defecto en la API
        searchArtists("rock") 
        getPlayer()
    }

    // NUEVA FUNCIÓN: Busca en la API de verdad usando Corrutinas
    fun searchArtists(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
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
                Log.i("SoundConnect","Error: ${error.message}")
                close(error.toException())
            }
        }
        val ref = database.reference.child("player")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private fun getPlayer(){
        viewModelScope.launch {
            collectPlayer().collect {
                val player = it.getValue(Player::class.java)
                _player.value = player
            }
        }
    }

    fun onPlaySelected() {
        if(player.value!= null){
            val currentPlayer = _player.value?.copy(play = !player.value?.play!!)
            val ref = database.reference.child("player")
            ref.setValue(currentPlayer)
        }
    }

    fun onCancelSelected(){
        val ref = database.reference.child("player")
        ref.setValue(null)
    }

    fun addPlayer(artist: Artist) {
        val ref = database.reference.child("player")
        val player = Player(artist,play=true)
        ref.setValue(player)
    }
}
