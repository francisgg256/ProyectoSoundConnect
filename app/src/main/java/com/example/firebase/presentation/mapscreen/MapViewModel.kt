package com.example.firebase.presentation.mapscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.firebase.data.model.MusicTag
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MapViewModel : ViewModel() {

    private var database: FirebaseDatabase = Firebase.database("https://soundconnect-3c760-default-rtdb.europe-west1.firebasedatabase.app")

    private val _musicTags = MutableStateFlow<List<MusicTag>>(emptyList())
    val musicTags: StateFlow<List<MusicTag>> = _musicTags

    private var valueEventListener: ValueEventListener? = null
    private var currentRef: com.google.firebase.database.DatabaseReference? = null

    init {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            if (uid != null) {
                getAllMusicTags()
            } else {
                removeListener()
                _musicTags.value = emptyList() 
            }
        }
    }

    fun addMusicTag(artistName: String, latLng: LatLng) {
        val user = Firebase.auth.currentUser
        val uid = user?.uid ?: return

        val ref = database.reference.child("music_tags").push()
        val senderName = user.displayName?.takeIf { it.isNotBlank() } ?: "Amante de la música"

        val newTag = MusicTag(
            id = ref.key ?: "",
            artistName = artistName,
            lat = latLng.latitude,
            lng = latLng.longitude,
            userName = senderName,
            userId = uid
        )
        ref.setValue(newTag)
    }

    private fun getAllMusicTags() {
        removeListener() 

        currentRef = database.reference.child("music_tags")

        valueEventListener = currentRef?.addValueEventListener(object : ValueEventListener {
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

    private fun removeListener() {
        valueEventListener?.let { listener ->
            currentRef?.removeEventListener(listener)
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeListener()
    }
}