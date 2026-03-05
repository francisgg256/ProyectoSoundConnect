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
    
    // Posición inicial (por ejemplo, el centro de Madrid o tu ciudad)
    val madrid = LatLng(40.4167, -3.7037)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(madrid, 12f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Dibujamos todas las chinchetas que haya en el ViewModel
        tags.forEach { tag ->
            Marker(
                state = MarkerState(position = tag.position),
                title = tag.artistName,
                snippet = "Escuchado aquí"
            )
        }
    }
}