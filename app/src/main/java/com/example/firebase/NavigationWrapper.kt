package com.example.firebase

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.firebase.presentation.auth.AuthViewModel
import com.example.firebase.presentation.homescreen.HomeScreen
import com.example.firebase.presentation.homescreen.HomeViewmodel
import com.example.firebase.presentation.initial.InitialScreen
import com.example.firebase.presentation.login.LoginScreen
import com.example.firebase.presentation.signup.SignupScreen

@Composable
fun NavigationWrapper(
    navHostController: NavHostController, 
    authViewModel: AuthViewModel, 
    homeViewModel: HomeViewmodel
) {
    // RA 1: Arquitectura robusta - El destino inicial se decide por el estado de la sesión
    val startRoute = if (authViewModel.isUserLoggedIn()) "home" else "Initial"

    NavHost(navHostController, startDestination = startRoute) {
        composable("Initial") {
            InitialScreen(
                navigateToLogin = { navHostController.navigate("Login") },
                navigateToSignUp = { navHostController.navigate("Signup") }
            )
        }
        composable("Login") {
            LoginScreen(
                viewModel = authViewModel, 
                navigateToHome = { 
                    navHostController.navigate("home") {
                        popUpTo("Initial") { inclusive = true }
                    }
                },
                navigateBack = { navHostController.popBackStack() }
            )
        }
        composable("Signup") {
            SignupScreen(
                viewModel = authViewModel, 
                navigateToHome = {
                    navHostController.navigate("home") {
                        popUpTo("Initial") { inclusive = true }
                    }
                },
                navigateBack = { navHostController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen(homeViewModel)
        }
    }
}
