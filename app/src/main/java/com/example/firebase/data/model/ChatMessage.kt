package com.example.firebase.data.model

data class ChatMessage(
    val id: String = "",//guardamos el identificador del mensaje
    val userName: String = "Usuario Anónimo",//el nombre de quien envia el mensaje
    val text: String = "",//el texto del mensaje
    val timestamp: Long = 0L//la fecha y hora en la que se mando el mensaje para poder ordenarlos
)
