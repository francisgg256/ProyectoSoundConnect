package com.example.firebase.presentation.auth

import androidx.lifecycle.ViewModel
import com.example.firebase.data.repository.AuthRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun clearError() {
        _errorMessage.value = null
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank() || password.isBlank()) {
            _errorMessage.value = "Por favor, rellena todos los campos."
            return 
        }

        repository.login(cleanEmail, password) { success ->
            if (success) {
                onSuccess()
            } else {
                _errorMessage.value = "Error al iniciar sesión: Correo o contraseña incorrectos."
                onError()
            }
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank() || password.isBlank()) {
            _errorMessage.value = "Por favor, rellena todos los campos."
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "La contraseña debe tener al menos 6 caracteres."
            return
        }

        repository.signUp(cleanEmail, password) { success ->
            if (success) {
                onSuccess()
            } else {
                _errorMessage.value = "Error al registrar. Por favor, inténtalo de nuevo."
                onError()
            }
        }
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit, onError: () -> Unit) {
        repository.loginWithGoogle(idToken) { success ->
            if (success) onSuccess() else onError()
        }
    }

    fun isUserLoggedIn(): Boolean = repository.getCurrentUser() != null

    fun logout() {
        Firebase.auth.signOut()
    }
}
