package com.example.firebase.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider // <-- AÑADE ESTE IMPORT

class AuthRepository(private val auth: FirebaseAuth) {

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    //recibe el email y la contraseña y onResult que compruebaa que el login haya ido bien o no
    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    //igual que el login pero crea el usuario en firebase
    fun signUp(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    //inicia sesion con google y el idtoken que convertimos en una credencial para firebase
    fun loginWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    //le dice a Firebase que borre la sesión activa del dispositivo
    fun signOut() {
        auth.signOut()
    }
}