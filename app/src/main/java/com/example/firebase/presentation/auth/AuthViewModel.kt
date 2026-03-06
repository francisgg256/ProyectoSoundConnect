package com.example.firebase.presentation.auth

import androidx.lifecycle.ViewModel
import com.example.firebase.data.repository.AuthRepository

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        repository.login(email, password) { success ->
            if (success) onSuccess() else onError()
        }
    }
    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        repository.signUp(email, password) { success ->
            if (success) onSuccess() else onError()
        }
    }
    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit, onError: () -> Unit) {
        repository.loginWithGoogle(idToken) { success ->
            if (success) onSuccess() else onError()
        }
    }
    fun isUserLoggedIn(): Boolean = repository.getCurrentUser() != null
}
