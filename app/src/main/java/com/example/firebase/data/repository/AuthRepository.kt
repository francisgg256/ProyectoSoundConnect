package com.example.firebase.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider // <-- AÑADE ESTE IMPORT

// La clase recibe por parámetro una instancia de FirebaseAuth.
// Se la pasamos desde fuera (desde el MainActivity) para poder reutilizarla.
class AuthRepository(private val auth: FirebaseAuth) {

    // Función sencilla que devuelve el usuario actual si hay alguien logueado, o 'null' si nadie ha iniciado sesión.
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // --- LOGIN CON CORREO Y CONTRASEÑA ---
    // Recibe el email, la contraseña, y una función "Lambda" llamada 'onResult'.
    // 'onResult: (Boolean) -> Unit' significa que, cuando termine de comprobar en internet,
    // devolverá un true (si el login fue bien) o un false (si la contraseña es incorrecta).
    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            // Como esto depende de internet, añade un 'Listener' que se ejecutará cuando Firebase responda.
            .addOnCompleteListener { task ->
                // task.isSuccessful será true si entró bien, y false si falló. Lo mandamos de vuelta a la pantalla.
                onResult(task.isSuccessful)
            }
    }

    // --- REGISTRO CON CORREO Y CONTRASEÑA ---
    // Funciona exactamente igual que el login, pero usa 'createUserWithEmailAndPassword'
    // para registrar el correo por primera vez en la base de datos de Firebase.
    fun signUp(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // --- LOGIN CON GOOGLE ---
    // Para iniciar sesión con Google, no le pasamos email y contraseña, sino un 'idToken'.
    // Este token es un código cifrado que nos da la ventanita emergente de Google en el móvil.
    fun loginWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        // Convertimos ese token en una "Credencial" oficial que Firebase pueda entender.
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // Iniciamos sesión en Firebase usando esa credencial de Google.
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // --- CERRAR SESIÓN ---
    // Simplemente le dice a Firebase que borre la sesión activa del dispositivo.
    fun signOut() {
        auth.signOut()
    }
}