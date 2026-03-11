package com.example.firebase

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets // <-- NUEVO IMPORT
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    mapViewModel: MapViewModel,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navHostController,
        startDestination = Screens.Initial.route,
        modifier = Modifier
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding()
            )
            // ¡ESTA ES LA LÍNEA MÁGICA QUE EVITA EL HUECO BLANCO DEL CHAT!
            .consumeWindowInsets(paddingValues)
    ) {

        composable(Screens.Initial.route) {
            InitialScreen(
                viewModel = authViewModel,
                navigateToLogin = { navHostController.navigate(Screens.Login.route) },
                navigateToSignUp = { navHostController.navigate(Screens.Signup.route) },
                navigateToHome = {
                    navHostController.navigate(Screens.Home.route) { popUpTo(Screens.Initial.route) { inclusive = true } }
                }
            )
        }

        composable(Screens.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navHostController.navigate(Screens.Home.route) { popUpTo(Screens.Initial.route) { inclusive = true } }
                },
                navigateBack = { navHostController.popBackStack() }
            )
        }

        composable(Screens.Signup.route) {
            SignupScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navHostController.navigate(Screens.Home.route) { popUpTo(Screens.Initial.route) { inclusive = true } }
                },
                navigateBack = { navHostController.popBackStack() }
            )
        }

        composable(Screens.Home.route) { HomeScreen(viewmodel = homeViewModel) }

        composable(Screens.Map.route) { MapScreen(mapViewModel = mapViewModel, homeViewModel = homeViewModel) }

        composable(Screens.Chat.route) { ChatScreen(viewmodel = chatViewModel) }
    }
}