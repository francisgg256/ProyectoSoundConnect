package com.example.firebase

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

class MainActivity : ComponentActivity() {

    // Variables globales de la Activity. "lateinit" significa que las inicializaremos más tarde.
    private lateinit var navHostController: NavHostController

    // Construimos los Repositorios y ViewModels usando "by lazy" (solo se crean cuando se necesitan por primera vez).
    private val authRepository by lazy { AuthRepository(Firebase.auth) }
    private val authViewModel by lazy { AuthViewModel(authRepository) }

    private val appDatabase by lazy { AppDatabase.getDatabase(applicationContext) }
    private val musicRepository by lazy {
        MusicRepository(RetrofitClient.apiService, appDatabase.artistDao())
    }
    private val homeViewModel by lazy { HomeViewmodel(musicRepository) }
    
    // ViewModels específicos
    private val chatViewModel by lazy { ChatViewModel() }
    private val mapViewModel by lazy { MapViewModel() }

    private val themeViewModel = ThemeViewModel()

    private lateinit var shakeDetector: ShakeDetector
    private var accelerometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

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

                        if (currentRoute == Screens.Home.route || currentRoute == Screens.Map.route || currentRoute == Screens.Chat.route) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.List, contentDescription = "Música") },
                                    label = { Text("Música") },
                                    selected = currentRoute == Screens.Home.route,
                                    onClick = { navHostController.navigate(Screens.Home.route) { launchSingleTop = true } }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Mapa") },
                                    label = { Text("Mapa") },
                                    selected = currentRoute == Screens.Map.route,
                                    onClick = { navHostController.navigate(Screens.Map.route) { launchSingleTop = true } }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Email, contentDescription = "Chat") },
                                    label = { Text("Chat") },
                                    selected = currentRoute == Screens.Chat.route,
                                    onClick = { navHostController.navigate(Screens.Chat.route) { launchSingleTop = true } }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavigationWrapper(
                            navHostController = navHostController,
                            authViewModel = authViewModel,
                            homeViewModel = homeViewModel,
                            chatViewModel = chatViewModel,
                            mapViewModel = mapViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        themeViewModel.startListening(sensorManager)
        accelerometer?.let {
            sensorManager.registerListener(shakeDetector, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        themeViewModel.stopListening(sensorManager)
        sensorManager.unregisterListener(shakeDetector)
    }
}
