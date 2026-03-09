package com.example.firebase

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.firebase.presentation.auth.AuthViewModel
import com.example.firebase.presentation.homescreen.HomeScreen
import com.example.firebase.presentation.homescreen.HomeViewmodel
import com.example.firebase.presentation.initial.InitialScreen
import com.example.firebase.presentation.login.LoginScreen
import com.example.firebase.presentation.signup.SignupScreen
import com.example.firebase.presentation.mapscreen.MapScreen
import com.example.firebase.presentation.chatscreen.ChatScreen
import com.example.firebase.presentation.chatscreen.ChatViewModel
import com.example.firebase.presentation.mapscreen.MapViewModel

// 1. DEFINICIÓN DE RUTAS (Sealed Class)
// Usamos una 'sealed class' para que las rutas sean seguras.
// Es como un menú cerrado: solo puedes navegar a lo que está definido aquí.
sealed class Screens(val route: String) {
    object Initial : Screens("initial")
    object Login : Screens("login")
    object Signup : Screens("signup")
    object Home : Screens("home")
    object Map : Screens("map")
    object Chat : Screens("chat")
}

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewmodel,
    chatViewModel: ChatViewModel,
    mapViewModel: MapViewModel
) {
    // 2. EL CONTENEDOR NAVHOST
    // Definimos el 'startDestination': la primera pantalla que verá el usuario al abrir la app.
    NavHost(navController = navHostController, startDestination = Screens.Initial.route) {

        // --- PANTALLA INICIAL ---
        composable(Screens.Initial.route) {
            InitialScreen(
                viewModel = authViewModel,
                navigateToLogin = { navHostController.navigate(Screens.Login.route) },
                navigateToSignUp = { navHostController.navigate(Screens.Signup.route) },
                navigateToHome = {
                    // Al ir al Home, usamos popUpTo para limpiar el historial.
                    // 'inclusive = true' borra la pantalla inicial para que al darle atrás no vuelva al login.
                    navHostController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Initial.route) { inclusive = true }
                    }
                }
            )
        }

        // --- PANTALLA LOGIN ---
        composable(Screens.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navHostController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Initial.route) { inclusive = true }
                    }
                },
                navigateBack = { navHostController.popBackStack() } // Vuelve a la pantalla anterior
            )
        }

        // --- PANTALLA REGISTRO ---
        composable(Screens.Signup.route) {
            SignupScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navHostController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Initial.route) { inclusive = true }
                    }
                },
                navigateBack = { navHostController.popBackStack() }
            )
        }

        // --- PANTALLA PRINCIPAL (MÚSICA) ---
        composable(Screens.Home.route) {
            HomeScreen(viewmodel = homeViewModel)
        }

        // --- PANTALLA MAPA ---
        composable(Screens.Map.route) {
            // Pasamos ambos ViewModels porque el mapa necesita saber qué suena en el Home
            MapScreen(mapViewModel = mapViewModel, homeViewModel = homeViewModel)
        }

        // --- PANTALLA CHAT ---
        composable(Screens.Chat.route) {
            ChatScreen(viewmodel = chatViewModel)
        }
    }
}
