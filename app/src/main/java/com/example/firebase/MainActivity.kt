package com.example.firebase

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity // Necesario para la API de idiomas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat // <-- IMPORTANTE: Necesario para el fix del teclado
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.firebase.data.local.AppDatabase
import com.example.firebase.data.network.RetrofitClient
import com.example.firebase.data.repository.AuthRepository
import com.example.firebase.data.repository.MusicRepository
import com.example.firebase.presentation.ThemeViewModel
import com.example.firebase.presentation.auth.AuthViewModel
import com.example.firebase.presentation.chatscreen.ChatViewModel
import com.example.firebase.presentation.homescreen.HomeViewmodel
import com.example.firebase.presentation.mapscreen.MapViewModel
import com.example.firebase.presentation.sensors.ShakeDetector
import com.example.firebase.ui.theme.FirebaseTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var navHostController: NavHostController

    private val authRepository by lazy { AuthRepository(Firebase.auth) }
    private val authViewModel by lazy { AuthViewModel(authRepository) }

    private val appDatabase by lazy { AppDatabase.getDatabase(applicationContext) }
    private val musicRepository by lazy {
        MusicRepository(RetrofitClient.apiService, appDatabase.artistDao())
    }
    private val homeViewModel by lazy { HomeViewmodel(musicRepository) }

    private val chatViewModel by lazy { ChatViewModel() }
    private val mapViewModel by lazy { MapViewModel() }

    private val themeViewModel = ThemeViewModel()

    private lateinit var shakeDetector: ShakeDetector
    private var accelerometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawableResource(android.R.color.black)

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        shakeDetector = ShakeDetector {
            homeViewModel.recommendRandomArtist()
        }

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            navHostController = rememberNavController()

            FirebaseTheme(darkTheme = themeViewModel.isDarkTheme.value) {

                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navHostController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        val showBottomBar = currentRoute in listOf(
                            Screens.Home.route,
                            Screens.Map.route,
                            Screens.Chat.route
                        )

                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.List, contentDescription = stringResource(R.string.nav_music)) },
                                    label = { Text(stringResource(R.string.nav_music)) },
                                    selected = currentRoute == Screens.Home.route,
                                    onClick = { navHostController.navigate(Screens.Home.route) { launchSingleTop = true } }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.nav_map)) },
                                    label = { Text(stringResource(R.string.nav_map)) },
                                    selected = currentRoute == Screens.Map.route,
                                    onClick = { navHostController.navigate(Screens.Map.route) { launchSingleTop = true } }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Email, contentDescription = stringResource(R.string.nav_chat)) },
                                    label = { Text(stringResource(R.string.nav_chat)) },
                                    selected = currentRoute == Screens.Chat.route,
                                    onClick = { navHostController.navigate(Screens.Chat.route) { launchSingleTop = true } }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavigationWrapper(
                            navHostController = navHostController,
                            authViewModel = authViewModel,
                            homeViewModel = homeViewModel,
                            chatViewModel = chatViewModel,
                            mapViewModel = mapViewModel,
                            paddingValues = paddingValues
                        )
                    }
                }
            }
        }
    }


    override fun onResume() {
        try {
            super.onResume()
        } catch (e: ClassCastException) {
            // Workaround for MIUI ClassCastException in ActivityInjector
        }
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        themeViewModel.startListening(sensorManager)

        accelerometer?.let {
            sensorManager.registerListener(shakeDetector, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        try {
            super.onPause()
        } catch (e: ClassCastException) {
            // Workaround for MIUI ClassCastException in ActivityInjector
        }
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        themeViewModel.stopListening(sensorManager)
        sensorManager.unregisterListener(shakeDetector)
    }
}