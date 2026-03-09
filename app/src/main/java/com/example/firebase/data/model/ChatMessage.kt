package com.example.firebase.data.model

// Usamos 'data class' porque su único trabajo es transportar información del chat.
// No tiene funciones complejas, solo guarda datos.
data class ChatMessage(
    // 'id' es el identificador único del mensaje.
    // Lo inicializamos a "" (String vacío) por defecto. Cuando Firebase guarde el mensaje, le dará un ID real (ej: "-Nxyz12345").
    val id: String = "",

    // 'userName' es el nombre de la persona que envía el mensaje.
    // Si por algún error de Firebase no llega el nombre, la app pondrá "Usuario Anónimo" para no dar un error de 'null'.
    val userName: String = "Usuario Anónimo",

    // 'text' es el contenido real del mensaje (lo que escribe el usuario).
    val text: String = "",

    // 'timestamp' es la fecha y hora exacta en la que se envió el mensaje.
    // Se guarda como un número 'Long' (milisegundos desde 1970) porque es mucho más fácil de ordenar
    // matemáticamente que guardar un texto como "12/03/2026 15:30".
    val timestamp: Long = 0L
)
