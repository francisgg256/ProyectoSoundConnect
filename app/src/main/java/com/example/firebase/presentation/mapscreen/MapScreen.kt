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
    val tags by viewmodel.musicTags.collectAsState()
    val currentPlayer by viewmodel.player.collectAsState()
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.4167, -3.7037), 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        // Al hacer clic, añadimos el artista actual a esa posición
        onMapClick = { latLng ->
            val artistName = currentPlayer?.artist?.name ?: "Música desconocida"
            viewmodel.addMusicTag(artistName, latLng)
        }
    ) {
        // Dibujamos todos los marcadores bajados de Firebase
        tags.forEach { tag ->
            Marker(
                // Convertimos el lat y lng de Firebase a LatLng de Google Maps
                state = MarkerState(position = LatLng(tag.lat, tag.lng)),
                title = tag.artistName,
                snippet = "Guardado por ${tag.userName}"
            )
        }
    }
}