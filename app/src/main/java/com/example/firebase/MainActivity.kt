package com.example.firebase

import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.firebase.data.repository.AuthRepository
import com.example.firebase.presentation.ThemeViewModel
import com.example.firebase.presentation.auth.AuthViewModel
import com.example.firebase.presentation.homescreen.HomeViewmodel
import com.example.firebase.ui.theme.FirebaseTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private lateinit var navHostController: NavHostController
    
    // Instanciamos el repositorio y los ViewModels
    private val authRepository by lazy { AuthRepository(Firebase.auth) }
    private val authViewModel by lazy { AuthViewModel(authRepository) }
    private val homeViewModel = HomeViewmodel()
    private val themeViewModel = ThemeViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            navHostController = rememberNavController()
            // RA 3: El tema de la app responde dinámicamente al sensor de luz
            FirebaseTheme(darkTheme = themeViewModel.isDarkTheme.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pasamos el authViewModel en lugar del objeto auth directo
                    NavigationWrapper(navHostController, authViewModel, homeViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Iniciamos la escucha del sensor de luz
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        themeViewModel.startListening(sensorManager)
    }

    override fun onPause() {
        super.onPause()
        // Detenemos la escucha para ahorrar batería (Buenas prácticas)
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        themeViewModel.stopListening(sensorManager)
    }
}
