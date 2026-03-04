package com.example.firebase.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository(private val auth: FirebaseAuth) {

    // Comprobar si hay un usuario logueado (para tu NavigationWrapper)
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // Lógica de Inicio de Sesión
    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // Lógica de Registro
    fun signUp(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // Lógica de Cerrar Sesión
    fun signOut() {
        auth.signOut()
    }
}
