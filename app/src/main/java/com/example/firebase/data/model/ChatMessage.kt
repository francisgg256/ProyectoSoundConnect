package com.example.firebase.data.model

data class ChatMessage(
    //le damos un id al mensaje
    val id: String = "",

    //el nombre del usuario que ha escrito el mensaje
    val userName: String = "Usuario Anónimo",

    // el contenido del mensaje
    val text: String = "",

    //la fecha y hora exacta a la que se ha escrito el mensaje
    val timestamp: Long = 0L
)
