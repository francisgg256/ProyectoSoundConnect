package com.example.firebase.presentation.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

// Recibe por parámetro "onShake", que es la acción que queremos hacer cuando se agite (recomendar artista).
class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    // Guarda la última vez que agitamos el móvil.
    private var lastShakeTime: Long = 0

    // Se dispara docenas de veces por segundo con los datos del movimiento del móvil.
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // El acelerómetro da la fuerza de gravedad en 3 ejes: X (izquierda/derecha), Y (arriba/abajo), Z (adelante/atrás).
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Convertimos esos números a la fuerza de la gravedad estándar (g).
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            // Fórmula matemática para calcular la fuerza total combinada de los 3 ejes.
            val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

            // Si la fuerza total es mayor que 2.5G (eso es un buen meneo, no un movimiento accidental):
            if (gForce > 2.5f) {
                val now = System.currentTimeMillis()

                // Comprobamos si ha pasado más de 1 segundo y medio (1500ms) desde el último meneo.
                // ¿Por qué? Porque si no, ¡al agitar el móvil se dispararía 50 veces seguidas!
                if (now - lastShakeTime > 1500) {
                    lastShakeTime = now // Guardamos la hora de este meneo.
                    onShake() // ¡Avisamos al MainActivity para que ponga un artista aleatorio!
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}