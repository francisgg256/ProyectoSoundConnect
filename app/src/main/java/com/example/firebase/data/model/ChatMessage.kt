package com.example.firebase.data.model

data class ChatMessage(
    val id: String = "",

    val userName: String = "Usuario Anónimo",

    val text: String = "",

    val timestamp: Long = 0L
)
