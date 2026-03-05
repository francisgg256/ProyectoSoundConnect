package com.example.firebase.presentation.chatscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebase.presentation.homescreen.HomeViewmodel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(viewmodel: HomeViewmodel) {
    // Obtenemos todos los mensajes de Firebase
    val messages by viewmodel.chatMessages.collectAsState()
    var textToSend by remember { mutableStateOf("") } // El texto que escribimos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Título superior
        Text(
            text = "Chat Global",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Lista de burbujas de chat
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                MessageBubble(
                    sender = msg.userName,
                    text = msg.text,
                    timestamp = msg.timestamp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Zona para escribir y enviar (Barra inferior)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = textToSend,
                onValueChange = { textToSend = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.DarkGray,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp) // Redondea la caja de texto
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Botón redondo con el icono de enviar
            IconButton(
                onClick = {
                    viewmodel.sendMessage(textToSend) // Llama al ViewModel para enviar
                    textToSend = "" // Vacía la barra de texto
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
            }
        }
    }
}

// Dibuja el estilo tipo "WhatsApp" para cada mensaje
@Composable
fun MessageBubble(sender: String, text: String, timestamp: Long) {
    // Convierte los milisegundos gigantes en horas y minutos (Ej: 14:30)
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = sdf.format(Date(timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Nombre del que envía el mensaje
        Text(
            text = sender,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        // La caja o globo del mensaje
        Box(
            modifier = Modifier
                // clip recorta 3 esquinas y deja 1 recta para que parezca un bocadillo
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                .background(Color.Gray.copy(alpha = 0.2f))
                .padding(12.dp)
        ) {
            Column {
                Text(text = text, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp) // El mensaje
                Text(
                    text = timeString, // La hora
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End) // Alinea la hora a la derecha
                )
            }
        }
    }
}
