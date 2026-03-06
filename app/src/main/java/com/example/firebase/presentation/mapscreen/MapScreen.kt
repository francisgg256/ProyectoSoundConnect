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

@SuppressLint("MissingPermission") // Ignora el aviso de permisos porque ya los pedimos en ejecución
@Composable
fun MapScreen(mapViewModel: MapViewModel, homeViewModel: HomeViewmodel) {
    val context = LocalContext.current
    val tags by mapViewModel.musicTags.collectAsState()
    val currentPlayer by homeViewModel.player.collectAsState()

    // Variable para saber si tenemos permiso de GPS
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Herramienta para pedir permisos múltiples (GPS y Notificaciones)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    // Lista para no repetir la misma notificación todo el rato
    val notifiedTags = remember { mutableStateListOf<String>() }

    // Posición inicial del mapa
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.4167, -3.7037), 10f)
    }

    // Al abrir la pantalla, pedimos los permisos y comprobamos la distancia
    LaunchedEffect(tags) {
        // 1. Preparamos los permisos a pedir
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        // El permiso de notificaciones solo es necesario en Android 13+ (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Pedimos permisos
        permissionLauncher.launch(permissionsToRequest.toTypedArray())

        // 2. Si hay tags, buscamos nuestra ubicación
        if (tags.isNotEmpty()) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    tags.forEach { tag ->
                        // Calculamos la distancia matemática entre el usuario y la chincheta
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            location.latitude, location.longitude,
                            tag.lat, tag.lng,
                            results
                        )
                        val distanceInMeters = results[0]

                        // Si está a menos de 200 metros y no le hemos notificado ya...
                        if (distanceInMeters < 200f && !notifiedTags.contains(tag.id)) {
                            showProximityNotification(context, tag.artistName) // ¡NOTIFICACIÓN!
                            notifiedTags.add(tag.id) // Lo marcamos para no agobiarle
                        }
                    }
                }
            }
        }
    }

    // Lienzo del mapa de Google
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        // Si tenemos permiso, mostramos el punto azul de ubicación actual
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
            // Abre el cartelito del nombre automáticamente
            LaunchedEffect(markerState) {
                markerState.showInfoWindow()
            }
        }
    }
}
