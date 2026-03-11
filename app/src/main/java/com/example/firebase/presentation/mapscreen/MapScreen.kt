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
import androidx.compose.ui.res.stringResource
import com.example.firebase.R
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

// @SuppressLint("MissingPermission") se usa porque gestionamos los permisos manualmente
// antes de activar funciones que requieren GPS.
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(mapViewModel: MapViewModel, homeViewModel: HomeViewmodel) {
    // Obtenemos el contexto para usar servicios de ubicación y lanzar notificaciones.
    val context = LocalContext.current

    // Observamos los marcadores musicales y el reproductor actual desde los ViewModels.
    val tags by mapViewModel.musicTags.collectAsState()
    val currentPlayer by homeViewModel.player.collectAsState()

    // Estado para controlar si el usuario ha aceptado los permisos de ubicación.
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Lanzador para solicitar múltiples permisos (GPS y Notificaciones).
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Si se concede la ubicación precisa, actualizamos el estado.
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    // Lista para recordar qué notificaciones ya se han enviado y no repetirlas.
    val notifiedTags = remember { mutableStateListOf<String>() }

    // Configuramos la posición inicial de la cámara del mapa (coordenadas por defecto).
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.4167, -3.7037), 10f)
    }

    // --- 1. PEDIR PERMISOS SOLO UNA VEZ ---
    // LaunchedEffect(Unit) se dispara SOLO cuando la pantalla se abre por primera vez.
    LaunchedEffect(Unit) {
        // Preparamos la lista de permisos necesaria según la versión de Android.
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        // A partir de Android 13 (Tiramisu), es obligatorio pedir permiso de notificaciones.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Lanzamos la solicitud de permisos.
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // --- 2. COMPROBAR DISTANCIAS CADA VEZ QUE HAY NUEVAS CHINCHETAS ---
    // LaunchedEffect(tags) se dispara cuando cambian los marcadores (tags).
    LaunchedEffect(tags) {
        // LÓGICA DE GEOFENCING: Si hay marcadores Y TENEMOS PERMISO, comprobamos nuestra ubicación actual.
        if (tags.isNotEmpty() && hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    tags.forEach { tag ->
                        val results = FloatArray(1)
                        // Calculamos la distancia entre el usuario y cada marcador.
                        android.location.Location.distanceBetween(
                            location.latitude, location.longitude,
                            tag.lat, tag.lng,
                            results
                        )
                        val distanceInMeters = results[0]

                        // Si la distancia es menor a 200m y no hemos notificado este marcador...
                        if (distanceInMeters < 200f && !notifiedTags.contains(tag.id)) {
                            // Mostramos la notificación del sistema.
                            showProximityNotification(context, tag.artistName)
                            notifiedTags.add(tag.id)
                        }
                    }
                }
            }
        }
    }

    // Guardamos el valor por defecto en una variable para evitar recalcularlo
    val unknownMusic = stringResource(R.string.unknown_music)

    // Componente principal de Google Maps.
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        // isMyLocationEnabled activa el punto azul si hay permisos de GPS.
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
        onMapClick = { latLng ->
            // Al hacer clic en el mapa, añadimos un marcador con el artista actual.
            val artistName = currentPlayer?.artist?.name ?: unknownMusic
            mapViewModel.addMusicTag(artistName, latLng)
        }
    ) {
        // Dibujamos cada marcador musical guardado en Firebase.
        tags.forEach { tag ->
            val markerState = rememberMarkerState(position = LatLng(tag.lat, tag.lng))
            Marker(
                state = markerState,
                title = tag.artistName,
                snippet = stringResource(R.string.saved_by, tag.userName)
            )
            // Forzamos a que el cuadro de información del marcador esté visible.
            LaunchedEffect(markerState) {
                markerState.showInfoWindow()
            }
        }
    }
}