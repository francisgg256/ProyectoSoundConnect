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

// Definición de las rutas
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
    homeViewModel: HomeViewmodel
) {
    NavHost(navController = navHostController, startDestination = Screens.Initial.route) {
        composable(Screens.Initial.route) {
            InitialScreen(
                navigateToLogin = { navHostController.navigate(Screens.Login.route) },
                navigateToSignUp = { navHostController.navigate(Screens.Signup.route) }
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
