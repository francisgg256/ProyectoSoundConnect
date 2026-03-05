package com.example.firebase.presentation.mapscreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.firebase.presentation.homescreen.HomeViewmodel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(viewmodel: HomeViewmodel) {
    // Obtenemos los datos del ViewModel
    val tags by viewmodel.musicTags.collectAsState()
    val currentPlayer by viewmodel.player.collectAsState()

    // Posición inicial del mapa (Madrid)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.4167, -3.7037), 10f)
    }

    // Lienzo del mapa de Google
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        // Acción al tocar una parte vacía del mapa:
        onMapClick = { latLng ->
            // Cogemos la canción actual o ponemos "Música desconocida"
            val artistName = currentPlayer?.artist?.name ?: "Música desconocida"
            viewmodel.addMusicTag(artistName, latLng) // Subimos la chincheta
        }
    ) {
        // Dibujamos todas las chinchetas (Markers) que hay guardadas en Firebase
        tags.forEach { tag ->
            Marker(
                state = MarkerState(position = LatLng(tag.lat, tag.lng)),
                title = tag.artistName,
                snippet = "Guardado por ${tag.userName}"
            )
        }
    }
}