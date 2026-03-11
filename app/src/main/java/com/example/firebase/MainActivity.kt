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

// Heredamos de AppCompatActivity para soportar el cambio de idioma en vivo y compatibilidad de temas
class MainActivity : AppCompatActivity() {

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

        // --- SOLUCIÓN PARA EL TROZO BLANCO (MÓVILES REALES) ---
        // 1. Forzamos fondo negro en la ventana para evitar destellos blancos
        window.setBackgroundDrawableResource(android.R.color.black)

        // 2. Habilita dibujo Edge to Edge
        enableEdgeToEdge()

        // 3. FIX DEFINITIVO: Desactivamos el ajuste automático del sistema.
        // Esto permite que Compose (imePadding) maneje el teclado sin interferencias blancas del SO.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Inicializamos el detector de agitación.
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

                // Scaffold es la estructura básica
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
                                    icon = { Icon(Icons.Default.List, contentDescription = stringResource(R.string.nav_music)) },
                                    label = { Text(stringResource(R.string.nav_music)) },
                                    selected = currentRoute == Screens.Home.route,
                                    onClick = { navHostController.navigate(Screens.Home.route) { launchSingleTop = true } }
                                )
                                // Item: Mapa
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.nav_map)) },
                                    label = { Text(stringResource(R.string.nav_map)) },
                                    selected = currentRoute == Screens.Map.route,
                                    onClick = { navHostController.navigate(Screens.Map.route) { launchSingleTop = true } }
                                )
                                // Item: Chat
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
                    // El contenido principal de la app
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Invocamos al Wrapper que contiene todas las rutas y le pasamos los paddingValues
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