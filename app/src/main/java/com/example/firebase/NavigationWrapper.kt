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

// "sealed class" es como una lista cerrada de opciones.
// Aquí definimos los "nombres" oficiales (rutas) de todas nuestras pantallas.
sealed class Screens(val route: String) {
    object Initial : Screens("initial") // La pantalla de bienvenida
    object Login : Screens("login")     // La pantalla de inicio de sesión
    object Signup : Screens("signup")   // La pantalla de registro
    object Home : Screens("home")       // La pantalla principal (Música)
    object Map : Screens("map")         // La pantalla del mapa
    object Chat : Screens("chat")       // La pantalla del chat global
}

@Composable
fun NavigationWrapper(
    navHostController: NavHostController, // El "conductor" que nos lleva de una pantalla a otra.
    authViewModel: AuthViewModel,         // El "cerebro" para las contraseñas.
    homeViewModel: HomeViewmodel          // El "cerebro" para la música, mapas y chat.
) {
    // NavHost es el contenedor principal. Le decimos que empiece en la ruta "Initial" (Bienvenida).
    NavHost(navController = navHostController, startDestination = Screens.Initial.route) {

        // composable(...) define una parada en nuestro mapa de carreteras.
        composable(Screens.Initial.route) {
            // Si estamos en "initial", mostramos la InitialScreen.
            InitialScreen(
                viewModel = authViewModel, // Le pasamos el cerebro
                // Le damos las instrucciones de qué hacer si pulsan los botones:
                navigateToLogin = { navHostController.navigate(Screens.Login.route) },
                navigateToSignUp = { navHostController.navigate(Screens.Signup.route) },
                navigateToHome = { 
                    navHostController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Initial.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screens.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    // Si el login es correcto, vamos a "home"...
                    navHostController.navigate(Screens.Home.route) {
                        // ... y "popUpTo(Initial)" significa que borramos el historial para que si pulsa "Atrás", salga de la app en vez de volver a la pantalla de Login.
                        popUpTo(Screens.Initial.route) { inclusive = true }
                    }
                },
                navigateBack = { navHostController.popBackStack() } // Vuelve a la pantalla anterior.
            )
        }

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

        // Las tres pantallas principales a las que llegamos tras el login:
        composable(Screens.Home.route) {
            HomeScreen(viewmodel = homeViewModel) // Le pasamos el HomeViewModel para que tenga datos.
        }
        composable(Screens.Map.route) {
            MapScreen(viewmodel = homeViewModel)
        }
        composable(Screens.Chat.route) {
            ChatScreen(viewmodel = homeViewModel)
        }
    }
}
