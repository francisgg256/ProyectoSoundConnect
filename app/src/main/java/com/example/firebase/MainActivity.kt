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
import com.example.firebase.presentation.homescreen.HomeViewmodel
import com.example.firebase.presentation.sensors.ShakeDetector
import com.example.firebase.ui.theme.FirebaseTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    // Variables globales de la Activity. "lateinit" significa que las inicializaremos más tarde.
    private lateinit var navHostController: NavHostController

    // Construimos los Repositorios y ViewModels usando "by lazy" (solo se crean cuando se necesitan por primera vez).
    // Conectamos Firebase Auth con el AuthRepository y luego con el AuthViewModel.
    private val authRepository by lazy { AuthRepository(Firebase.auth) }
    private val authViewModel by lazy { AuthViewModel(authRepository) }

    // Conectamos la base de datos (AppDatabase) y Retrofit con el MusicRepository, y luego con el HomeViewModel.
    private val appDatabase by lazy { AppDatabase.getDatabase(applicationContext) }
    private val musicRepository by lazy {
        MusicRepository(RetrofitClient.apiService, appDatabase.artistDao())
    }
    private val homeViewModel by lazy { HomeViewmodel(musicRepository) }

    // ViewModel para controlar el tema claro/oscuro con el sensor de luz.
    private val themeViewModel = ThemeViewModel()

    // Variables para el acelerómetro (agitar el móvil).
    private lateinit var shakeDetector: ShakeDetector
    private var accelerometer: Sensor? = null

    // onCreate es la función más importante de Android: se ejecuta al abrir la app.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Hace que la app ocupe toda la pantalla (hasta la zona de la batería/reloj).

        // Preparamos el detector de agitación. Si el usuario agita, llamamos a recommendRandomArtist()
        shakeDetector = ShakeDetector {
            homeViewModel.recommendRandomArtist()
        }

        // Le pedimos a Android acceso al Acelerómetro.
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // setContent es donde empezamos a dibujar la interfaz con Jetpack Compose.
        setContent {
            navHostController = rememberNavController() // Creamos el conductor para navegar.

            // Aplicamos nuestro Tema (que cambiará de claro a oscuro si el ThemeViewModel lo dice).
            FirebaseTheme(darkTheme = themeViewModel.isDarkTheme.value) {
                // Scaffold es una plantilla de pantalla que nos permite poner barras arriba o abajo muy fácilmente.
                Scaffold(
                    // Aquí configuramos la barra de navegación inferior (bottomBar).
                    bottomBar = {
                        // Preguntamos en qué ruta (pantalla) estamos actualmente.
                        val navBackStackEntry by navHostController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        // ¡Importante! Solo mostramos la barra inferior si NO estamos en Login o Initial.
                        if (currentRoute == Screens.Home.route || currentRoute == Screens.Map.route || currentRoute == Screens.Chat.route) {
                            NavigationBar {
                                // Botón 1: Música
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.List, contentDescription = "Música") },
                                    label = { Text("Música") },
                                    selected = currentRoute == Screens.Home.route, // Se ilumina si estamos en Home
                                    onClick = { navHostController.navigate(Screens.Home.route) { launchSingleTop = true } } // launchSingleTop evita abrir la pantalla 10 veces si pulsamos 10 veces.
                                )
                                // Botón 2: Mapa
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Mapa") },
                                    label = { Text("Mapa") },
                                    selected = currentRoute == Screens.Map.route,
                                    onClick = { navHostController.navigate(Screens.Map.route) { launchSingleTop = true } }
                                )
                                // Botón 3: Chat
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Email, contentDescription = "Chat") },
                                    label = { Text("Chat") },
                                    selected = currentRoute == Screens.Chat.route,
                                    onClick = { navHostController.navigate(Screens.Chat.route) { launchSingleTop = true } }
                                )
                            }
                        }
                    }
                ) { paddingValues -> // paddingValues es el espacio que ocupa la barra inferior para que no tape el contenido.

                    // Surface es el "lienzo" donde dibujamos.
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues), // Respetamos el espacio de la barra inferior.
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Aquí llamamos al "Mapa de Carreteras" y le damos las herramientas para funcionar.
                        NavigationWrapper(navHostController, authViewModel, homeViewModel)
                    }
                }
            }
        }
    }

    // onResume se ejecuta cuando la app vuelve a primer plano (si sales al escritorio y vuelves a entrar).
    override fun onResume() {
        super.onResume()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Volvemos a encender los sensores para no gastar batería cuando la app está minimizada.
        themeViewModel.startListening(sensorManager) // Sensor de luz
        accelerometer?.let {
            sensorManager.registerListener(shakeDetector, it, SensorManager.SENSOR_DELAY_UI) // Acelerómetro
        }
    }

    // onPause se ejecuta cuando la app se minimiza o se cambia a otra app.
    override fun onPause() {
        super.onPause()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Apagamos los sensores para ahorrar batería.
        themeViewModel.stopListening(sensorManager)
        sensorManager.unregisterListener(shakeDetector)
    }
}
