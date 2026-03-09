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
import androidx.compose.material3.*
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

    // --- 1. PROPIEDADES DE NAVEGACIÓN Y ARQUITECTURA ---
    private lateinit var navHostController: NavHostController

    // Inyección de Dependencias Manual (usando 'by lazy' para ahorrar memoria hasta que se necesiten)
    private val authRepository by lazy { AuthRepository(Firebase.auth) }
    private val authViewModel by lazy { AuthViewModel(authRepository) }

    private val appDatabase by lazy { AppDatabase.getDatabase(applicationContext) }
    private val musicRepository by lazy {
        MusicRepository(RetrofitClient.apiService, appDatabase.artistDao())
    }
    private val homeViewModel by lazy { HomeViewmodel(musicRepository) }

    private val chatViewModel by lazy { ChatViewModel() }
    private val mapViewModel by lazy { MapViewModel() }

    // ViewModel que controla el tema claro/oscuro dinámico (Sensor de Luz)
    private val themeViewModel = ThemeViewModel()

    // --- 2. PROPIEDADES DE SENSORES (ACELERÓMETRO) ---
    private lateinit var shakeDetector: ShakeDetector
    private var accelerometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita que el contenido se dibuje bajo las barras del sistema (Edge to Edge)
        enableEdgeToEdge()

        // Inicializamos el detector de agitación.
        // Cuando detecte un "shake", llamamos a la función de recomendación aleatoria.
        shakeDetector = ShakeDetector {
            homeViewModel.recommendRandomArtist()
        }

        // Accedemos al gestor de sensores del hardware
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            // Inicializamos el motor de navegación de Compose
            navHostController = rememberNavController()

            // Aplicamos nuestro Tema Personalizado.
            // darkTheme observa el valor del sensor de luz en tiempo real.
            FirebaseTheme(darkTheme = themeViewModel.isDarkTheme.value) {

                // Scaffold es la estructura básica (hueco para barra superior, inferior y contenido)
                Scaffold(
                    bottomBar = {
                        // Lógica para mostrar la barra solo en las pantallas principales
                        val navBackStackEntry by navHostController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        val showBottomBar = currentRoute in listOf(
                            Screens.Home.route,
                            Screens.Map.route,
                            Screens.Chat.route
                        )

                        if (showBottomBar) {
                            NavigationBar {
                                // Item: Música
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.List, contentDescription = "Música") },
                                    label = { Text("Música") },
                                    selected = currentRoute == Screens.Home.route,
                                    onClick = { navHostController.navigate(Screens.Home.route) { launchSingleTop = true } }
                                )
                                // Item: Mapa
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Mapa") },
                                    label = { Text("Mapa") },
                                    selected = currentRoute == Screens.Map.route,
                                    onClick = { navHostController.navigate(Screens.Map.route) { launchSingleTop = true } }
                                )
                                // Item: Chat
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
                    // El contenido principal de la app
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Invocamos al Wrapper que contiene todas las rutas
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

    // --- 3. CICLO DE VIDA Y SENSORES ---

    override fun onResume() {
        super.onResume()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Empezamos a escuchar al sensor de luz (Modo Oscuro)
        themeViewModel.startListening(sensorManager)

        // Empezamos a escuchar al acelerómetro (Agitar)
        accelerometer?.let {
            sensorManager.registerListener(shakeDetector, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // ¡IMPORTANTE! Liberamos los sensores para no drenar la batería
        themeViewModel.stopListening(sensorManager)
        sensorManager.unregisterListener(shakeDetector)
    }
}
