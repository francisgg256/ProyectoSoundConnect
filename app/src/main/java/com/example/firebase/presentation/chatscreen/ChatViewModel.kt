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

    private var database: FirebaseDatabase = Firebase.database("https://soundconnect-3c760-default-rtdb.europe-west1.firebasedatabase.app")

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    init {
        getChatMessages()
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val ref = database.reference.child("chat_messages").push()
        val newMessage = ChatMessage(
            id = ref.key ?: "",
            userName = "Amante de la música", // Aquí en el futuro podrías poner el nombre real del usuario logueado
            text = text,
            timestamp = System.currentTimeMillis()
        )

        ref.setValue(newMessage)
            .addOnSuccessListener { Log.i("SoundConnect", "Mensaje enviado al chat") }
            .addOnFailureListener { Log.e("SoundConnect", "Error al enviar mensaje: ${it.message}") }
    }

    private fun getChatMessages() {
        val ref = database.reference.child("chat_messages")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    val message = child.getValue(ChatMessage::class.java)
                    if (message != null) {
                        messagesList.add(message)
                    }
                }
                _chatMessages.value = messagesList.sortedBy { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SoundConnect", "Error al leer el chat: ${error.message}")
            }
        })
    }
}
