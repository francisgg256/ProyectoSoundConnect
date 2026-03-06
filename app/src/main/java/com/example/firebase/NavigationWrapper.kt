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
    navHostController: NavHostController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewmodel
) {
    NavHost(navController = navHostController, startDestination = Screens.Initial.route) {

        composable(Screens.Initial.route) {
            InitialScreen(
                viewModel = authViewModel,
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
                    navHostController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Initial.route) { inclusive = true }
                    }
                },
                navigateBack = { navHostController.popBackStack() }
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

        composable(Screens.Home.route) {
            HomeScreen(viewmodel = homeViewModel)
        }
        composable(Screens.Map.route) {
            MapScreen(viewmodel = homeViewModel)
        }
        composable(Screens.Chat.route) {
            ChatScreen(viewmodel = homeViewModel)
        }
    }
}
