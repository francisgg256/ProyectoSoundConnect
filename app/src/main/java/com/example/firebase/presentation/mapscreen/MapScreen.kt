package com.example.firebase.presentation.mapscreen

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.firebase.presentation.homescreen.HomeViewmodel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(mapViewModel: MapViewModel, homeViewModel: HomeViewmodel) {
    val context = LocalContext.current
    val tags by mapViewModel.musicTags.collectAsState()
    val currentPlayer by homeViewModel.player.collectAsState()

    var hasLocationPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    val notifiedTags = remember { mutableStateListOf<String>() }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.4167, -3.7037), 10f)
    }

    LaunchedEffect(tags) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissionLauncher.launch(permissionsToRequest.toTypedArray())

        if (tags.isNotEmpty()) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    tags.forEach { tag ->
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            location.latitude, location.longitude,
                            tag.lat, tag.lng,
                            results
                        )
                        val distanceInMeters = results[0]

                        if (distanceInMeters < 200f && !notifiedTags.contains(tag.id)) {
                            showProximityNotification(context, tag.artistName)
                            notifiedTags.add(tag.id)
                        }
                    }
                }
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
        onMapClick = { latLng ->
            val artistName = currentPlayer?.artist?.name ?: "Música desconocida"
            mapViewModel.addMusicTag(artistName, latLng) 
        }
    ) {
        tags.forEach { tag ->
            val markerState = rememberMarkerState(position = LatLng(tag.lat, tag.lng))
            Marker(
                state = markerState,
                title = tag.artistName,
                snippet = "Guardado por ${tag.userName}"
            )
            LaunchedEffect(markerState) {
                markerState.showInfoWindow()
            }
        }
    }
}