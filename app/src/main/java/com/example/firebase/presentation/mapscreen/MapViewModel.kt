package com.example.firebase.presentation.mapscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.firebase.data.model.MusicTag
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Heredamos de ViewModel para que los datos del mapa no se pierdan al rotar la pantalla.
class MapViewModel : ViewModel() {

    // 1. CONEXIÓN A LA BASE DE DATOS
    // Usamos la URL específica de Europa para asegurar la conexión con Firebase Realtime Database.
    private var database: FirebaseDatabase = Firebase.database("https://soundconnect-3c760-default-rtdb.europe-west1.firebasedatabase.app")

    // 2. GESTIÓN DEL ESTADO (STATEFLOW)
    // _musicTags es la lista privada y mutable donde guardaremos los marcadores.
    private val _musicTags = MutableStateFlow<List<MusicTag>>(emptyList())
    // musicTags es la versión pública e inmutable que la interfaz (MapScreen) observa para dibujar los pines.
    val musicTags: StateFlow<List<MusicTag>> = _musicTags

    // El bloque init ejecuta la lectura de datos nada más crearse el ViewModel.
    init {
        getMusicTags()
    }

    // 3. AÑADIR UN MARCADOR (CHINCHETA)
    // Recibe el nombre del artista actual y las coordenadas (Latitud/Longitud) donde se hizo clic.
    fun addMusicTag(artistName: String, latLng: LatLng) {
        // .push() crea una nueva ubicación única en Firebase para este marcador.
        val ref = database.reference.child("music_tags").push()

        // Creamos el objeto MusicTag con los datos recibidos.
        val newTag = MusicTag(
            id = ref.key ?: "", // Usamos la clave única generada por Firebase.
            artistName = artistName,
            lat = latLng.latitude,
            lng = latLng.longitude
        )
        // Subimos el marcador a la nube.
        ref.setValue(newTag)
    }

    // 4. LEER MARCADORES EN TIEMPO REAL
    private fun getMusicTags() {
        val ref = database.reference.child("music_tags")

        // addValueEventListener mantiene una conexión abierta: si alguien añade una chincheta,
        // aparecerá en el móvil de todos los usuarios al instante.
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tagsList = mutableListOf<MusicTag>()
                // Recorremos todos los hijos del nodo "music_tags".
                for (child in snapshot.children) {
                    // Convertimos el JSON de Firebase a nuestro modelo MusicTag.
                    val tag = child.getValue(MusicTag::class.java)
                    if (tag != null) tagsList.add(tag)
                }
                // Actualizamos el StateFlow para que la UI se refresque sola.
                _musicTags.value = tagsList
            }

            override fun onCancelled(error: DatabaseError) {
                // Si hay un error de permisos o red, lo registramos en el Log.
                Log.e("SoundConnect", "Error al leer chinchetas: ${error.message}")
            }
        })
    }
}
