package com.example.firebase.presentation.mapscreen

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.firebase.R

// Función global para mostrar una notificación de proximidad.
// Recibe el contexto de la app y el nombre del artista que se detectó cerca.
fun showProximityNotification(context: Context, artistName: String) {
    // 1. DEFINICIÓN DEL CANAL (Necesario a partir de Android 8.0 Oreo)
    val channelId = "music_proximity_channel"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Los "Canales de Notificación" permiten al usuario elegir qué tipo de avisos quiere recibir de la app.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Music Proximity", // Nombre que el usuario verá en los ajustes del móvil
            NotificationManager.IMPORTANCE_HIGH // Importancia alta para que aparezca como "pop-up" (HUD)
        )
        notificationManager.createNotificationChannel(channel)
    }

    // 2. CONSTRUCCIÓN DE LA NOTIFICACIÓN
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Icono que aparece en la barra de estado
        .setContentTitle(context.getString(R.string.proximity_title)) // Título principal
        .setContentText(context.getString(R.string.proximity_text, artistName)) // Texto detallado
        // Prioridad alta para versiones anteriores a Android 8.0
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        // Hace que la notificación desaparezca automáticamente cuando el usuario la pulsa
        .setAutoCancel(true)
        .build()

    // 3. LANZAMIENTO
    // Usamos el hashCode del nombre del artista como ID de la notificación.
    // Esto evita que si hay varios artistas cerca, una notificación pise a la otra.
    notificationManager.notify(artistName.hashCode(), notification)
}