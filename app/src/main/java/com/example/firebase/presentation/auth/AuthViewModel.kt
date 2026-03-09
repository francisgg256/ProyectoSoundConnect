package com.example.firebase.presentation.auth

import androidx.lifecycle.ViewModel
import com.example.firebase.data.repository.AuthRepository

// Heredamos de 'ViewModel()'. Esto es crucial porque hace que esta clase "sobreviva"
// si el usuario gira la pantalla del móvil, evitando que se pierdan los datos que estamos procesando.
// Además, recibe el AuthRepository por parámetro (Inyección de Dependencias).
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // --- FUNCIÓN DE LOGIN ---
    // Recibe el email, la contraseña y dos funciones "Lambda" o Callbacks:
    // 1. onSuccess: ¿Qué hacemos si sale bien? (ej. Navegar al Home)
    // 2. onError: ¿Qué hacemos si sale mal? (ej. Mostrar un mensaje de error)
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {

        // Llamamos al repositorio y le pasamos los datos.
        // El repositorio hace la llamada a Firebase en internet y nos devuelve un booleano ('success').
        repository.login(email, password) { success ->
            // Si Firebase dice 'true' (éxito), ejecutamos la función onSuccess().
            // Si dice 'false' (fallo de contraseña o usuario), ejecutamos onError().
            if (success) onSuccess() else onError()
        }
    }

    // --- FUNCIÓN DE REGISTRO ---
    // Funciona exactamente con la misma lógica que el login, pero llama a 'signUp' en el repositorio.
    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        repository.signUp(email, password) { success ->
            if (success) onSuccess() else onError()
        }
    }

    // --- FUNCIÓN DE LOGIN CON GOOGLE ---
    // Recibe el Token cifrado que nos da Google al seleccionar una cuenta en el móvil.
    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit, onError: () -> Unit) {
        repository.loginWithGoogle(idToken) { success ->
            if (success) onSuccess() else onError()
        }
    }

    // --- COMPROBAR SESIÓN ACTIVA ---
    // Devuelve un booleano instantáneo (true/false) comprobando si ya hay un usuario guardado.
    // Se usa al abrir la app para saber si mandamos al usuario al Login o directamente al Home.
    fun isUserLoggedIn(): Boolean = repository.getCurrentUser() != null
}
