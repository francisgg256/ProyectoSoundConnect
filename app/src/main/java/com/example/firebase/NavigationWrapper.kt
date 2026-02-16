package com.example.firebase

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.firebase.presentation.homescreen.HomeScreen
import com.example.firebase.presentation.homescreen.HomeViewmodel
import com.example.firebase.presentation.initial.InitialScreen
import com.example.firebase.presentation.login.LoginScreen
import com.example.firebase.presentation.signup.SignupScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NavigationWrapper(navHostController: NavHostController, auth: FirebaseAuth, homeViewModel: HomeViewmodel) {
    NavHost(navHostController,startDestination="Initial"){
        composable("Initial"){
            InitialScreen(
                navigateToLogin = {navHostController.navigate("Login")},
                navigateToSignUp = {navHostController.navigate("Signup")}
            )
        }
        composable("Login"){
            LoginScreen(auth, navigateToHome = {navHostController.navigate("home")})
        }
        composable("Signup"){
            SignupScreen(auth)
        }
        composable("home"){
            HomeScreen(homeViewModel)
        }
    }
}