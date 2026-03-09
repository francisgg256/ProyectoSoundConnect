package com.example.firebase.presentation.chatscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.firebase.data.model.ChatMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel : ViewModel() {

    // 1. CONEXIÓN A LA BASE DE DATOS
    // Inicializamos la conexión a Firebase. Le pasamos la URL exacta porque tu base de datos
    // está alojada en el servidor de Europa (europe-west1), y a veces Firebase necesita la URL manual para no perderse.
    private var database: FirebaseDatabase = Firebase.database("https://soundconnect-3c760-default-rtdb.europe-west1.firebasedatabase.app")

    // 2. GESTIÓN DEL ESTADO (STATEFLOW)
    // Usamos el patrón de "Encapsulación".
    // _chatMessages es MUTABLE (se puede modificar), pero es PRIVADA para que la pantalla no la rompa por error.
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    // chatMessages es PÚBLICA e INMUTABLE. La pantalla de Compose solo puede "leerla", pero no modificarla.
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    // El bloque 'init' se ejecuta AUTOMÁTICAMENTE en el mismo milisegundo en el que se crea este ViewModel.
    // Esto asegura que, nada más entrar a la pantalla, ya estemos descargando los mensajes.
    init {
        getChatMessages()
    }

    // 3. ENVIAR UN MENSAJE
    fun sendMessage(text: String) {
        // Medida de seguridad: Si el texto está vacío o son solo espacios, no hacemos nada.
        if (text.isBlank()) return

        // Entramos en la "carpeta" (child) "chat_messages".
        // La función .push() hace magia: crea una "subcarpeta" con un código único irrepetible (ej: -Nxyz987) para este mensaje.
        val ref = database.reference.child("chat_messages").push()

        // Construimos el objeto mensaje usando el modelo que creamos anteriormente.
        val newMessage = ChatMessage(
            id = ref.key ?: "", // Cogemos el código único que nos acaba de generar .push()
            userName = "Amante de la música", // Nombre hardcodeado (permitido según el PDF en textos estáticos)
            text = text,
            timestamp = System.currentTimeMillis() // Guardamos la hora exacta en milisegundos
        )

        // Subimos el objeto a la nube de Firebase.
        ref.setValue(newMessage)
            .addOnSuccessListener { Log.i("SoundConnect", "Mensaje enviado al chat") } // Si va bien, lo apuntamos en la consola
            .addOnFailureListener { Log.e("SoundConnect", "Error al enviar mensaje: ${it.message}") } // Si falla, mostramos el error
    }

    // 4. LEER LOS MENSAJES (EN TIEMPO REAL)
    private fun getChatMessages() {
        val ref = database.reference.child("chat_messages")

        // addValueEventListener es un "Oído" que se queda escuchando a la base de datos permanentemente.
        ref.addValueEventListener(object : ValueEventListener {

            // Esta función salta AUTOMÁTICAMENTE cada vez que alguien (tú o cualquier persona del mundo) envía un mensaje.
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<ChatMessage>()

                // Recorremos todos los mensajes que hay guardados en Firebase uno por uno
                for (child in snapshot.children) {
                    // Firebase convierte su JSON loco a nuestra clase de Kotlin automáticamente
                    val message = child.getValue(ChatMessage::class.java)
                    if (message != null) {
                        messagesList.add(message) // Lo metemos en nuestra lista temporal
                    }
                }

                // Actualizamos el StateFlow.
                // Usamos .sortedBy { it.timestamp } para asegurarnos matemáticamente de que
                // los mensajes más viejos salen arriba y los más nuevos abajo.
                _chatMessages.value = messagesList.sortedBy { it.timestamp }
            }

            // Si hay un error (ej. nos quedamos sin internet o sin permisos), salta esto.
            override fun onCancelled(error: DatabaseError) {
                Log.e("SoundConnect", "Error al leer el chat: ${error.message}")
            }
        })
    }
}
