package com.example.firebase.presentation.auth

import androidx.lifecycle.ViewModel
import com.example.firebase.data.repository.AuthRepository

// Le pasamos el AuthRepository por el constructor para que pueda pedirle cosas a Firebase.
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Función que llama la pantalla cuando el usuario pulsa "Iniciar Sesión".
    // Recibe el email, la contraseña y dos "acciones" (onSuccess y onError) que le dicen a la pantalla qué hacer después.
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        repository.login(email, password) { success -> // Llama al repositorio...
            if (success) onSuccess() else onError() // Si fue bien, ejecuta onSuccess (ir al Home). Si no, onError (mostrar error).
        }
    }

    // Exactamente igual que el login, pero para registrarse.
    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        repository.signUp(email, password) { success ->
            if (success) onSuccess() else onError()
        }
    }

    // Función rápida para saber si el usuario ya había iniciado sesión antes (para saltarse el login al abrir la app).
    fun isUserLoggedIn(): Boolean = repository.getCurrentUser() != null
}