package com.example.firebase.presentation.chatscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebase.R
import java.text.SimpleDateFormat
import java.util.*

// @Composable indica que esta función dibuja elementos en la pantalla.
@Composable
fun ChatScreen(viewmodel: ChatViewModel) {

    // 1. ESTADOS DE LA PANTALLA (STATE)
    // collectAsState(): Escucha en tiempo real el StateFlow del ViewModel.
    // Si entran mensajes nuevos desde Firebase, Compose redibujará la lista automáticamente.
    val messages by viewmodel.chatMessages.collectAsState()

    // mutableStateOf(""): Es una variable local que guarda lo que el usuario está escribiendo en la caja de texto.
    // Usamos 'remember' para que no se borre si la pantalla rota o se redibuja.
    var textToSend by remember { mutableStateOf("") }

    // Guardamos el "estado" del scroll de la lista. Nos sirve para saber por dónde va leyendo el usuario.
    val listState = rememberLazyListState()

    // 2. EFECTOS SECUNDARIOS (AUTO-SCROLL)
    // LaunchedEffect se ejecuta cada vez que el tamaño de la lista de mensajes (messages.size) cambia.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            // Cuando llega un mensaje nuevo, hacemos que la lista baje (scroll) automáticamente hasta el último mensaje.
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // 3. ESTRUCTURA PRINCIPAL (PANTALLA)
    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda la pantalla
            .background(MaterialTheme.colorScheme.background) // Usa el color de fondo del tema (Claro/Oscuro)
            .imePadding() // ¡MAGIA!: Esto empuja toda la pantalla hacia arriba cuando sale el teclado del móvil para que no tape la caja de texto.
    ) {
        // Título del Chat
        Text(
            text = stringResource(R.string.chat_global),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // 4. LISTA DE MENSAJES (EL "RECYCLERVIEW" DE COMPOSE)
        LazyColumn(
            state = listState, // Le pasamos el control del scroll que creamos arriba
            modifier = Modifier
                .weight(1f) // Esto hace que la lista ocupe todo el espacio sobrante, empujando la caja de texto abajo del todo
                .padding(horizontal = 16.dp),
            reverseLayout = false // Pone el primer mensaje arriba y el último abajo
        ) {
            // 'items' recorre la lista de mensajes y dibuja una 'MessageBubble' (burbuja) por cada uno
            items(messages) { msg ->
                MessageBubble(
                    sender = msg.userName,
                    text = msg.text,
                    timestamp = msg.timestamp
                )
                Spacer(modifier = Modifier.height(8.dp)) // Espacio entre mensajes
            }
        }

        // 5. BARRA INFERIOR (CAJA DE TEXTO Y BOTÓN DE ENVIAR)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically // Centra el botón y la caja de texto horizontalmente
        ) {
            // Caja donde el usuario escribe
            TextField(
                value = textToSend,
                onValueChange = { textToSend = it }, // Actualiza la variable cada vez que el usuario teclea una letra
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.chat_placeholder)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.DarkGray,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp) // Bordes muy redondeados
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Botón de Enviar
            IconButton(
                onClick = {
                    // isNotBlank evita que se envíen mensajes vacíos o solo con espacios
                    if (textToSend.isNotBlank()) {
                        viewmodel.sendMessage(textToSend) // Le dice al ViewModel que suba el texto a Firebase
                        textToSend = "" // Borramos la caja de texto para que el usuario pueda escribir otro
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Send, contentDescription = stringResource(R.string.chat_send), tint = Color.White)
            }
        }
    }
}

// 6. COMPONENTE EXTRA (LA BURBUJA DE MENSAJE)
// Lo sacamos a una función separada para que el código principal quede más limpio y fácil de leer.
@Composable
fun MessageBubble(sender: String, text: String, timestamp: Long) {

    // Formateamos el número largo del Timestamp a una hora legible (ej: "14:35")
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = sdf.format(Date(timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Nombre de la persona que lo envió
        Text(
            text = sender,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        // La caja con fondo gris que envuelve al texto
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)) // Hace un efecto "bocadillo" de cómic
                .background(Color.Gray.copy(alpha = 0.2f))
                .padding(12.dp)
        ) {
            Column {
                // El texto del mensaje
                Text(text = text, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                // La hora en pequeñito a la derecha
                Text(
                    text = timeString,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
