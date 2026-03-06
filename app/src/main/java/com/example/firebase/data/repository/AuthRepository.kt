package com.example.firebase.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider // <-- AÑADE ESTE IMPORT

// Pasamos la herramienta FirebaseAuth por el constructor para que el repositorio pueda usarla.
class AuthRepository(private val auth: FirebaseAuth) {

    // Función que pregunta si hay alguien logueado actualmente. Devuelve el usuario o "null" si nadie ha iniciado sesión.
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // Función para INICIAR SESIÓN.
    // Recibe el email, la contraseña y un "onResult" (una función que avisará a la pantalla si ha tenido éxito o no).
    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password) // Llama a Firebase
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful) // Si la tarea fue exitosa, devuelve "true". Si falló (mala contraseña), "false".
            }
    }

    // Función para REGISTRAR un nuevo usuario. Funciona igual que el login.
    fun signUp(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password) // Pide a Firebase crear la cuenta
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // --- AÑADE ESTA NUEVA FUNCIÓN PARA GOOGLE ---
    fun loginWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // Función para CERRAR SESIÓN.
    fun signOut() {
        auth.signOut()
    }
}
