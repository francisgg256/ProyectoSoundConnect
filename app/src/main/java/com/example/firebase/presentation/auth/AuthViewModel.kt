package com.example.firebase.presentation.auth

import androidx.lifecycle.ViewModel
import com.example.firebase.data.repository.AuthRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val cleanEmail = email.trim()

        repository.login(cleanEmail, password) { success ->
            if (success) onSuccess() else onError()
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val cleanEmail = email.trim()

        repository.signUp(cleanEmail, password) { success ->
            if (success) onSuccess() else onError()
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
