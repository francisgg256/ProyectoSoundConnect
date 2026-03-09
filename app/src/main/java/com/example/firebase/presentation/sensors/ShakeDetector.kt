package com.example.firebase.presentation.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

// Esta clase implementa 'SensorEventListener', lo que permite "escuchar" los sensores del hardware.
// Recibe por parámetro una función 'onShake' que se ejecutará cuando detectemos el movimiento.
class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    // Variable para evitar que la app se vuelva loca y detecte 50 agitaciones por segundo.
    // Solo permitiremos una acción cada 1.5 segundos.
    private var lastShakeTime: Long = 0

    // Esta función salta CADA VEZ que el acelerómetro detecta un micro-movimiento.
    override fun onSensorChanged(event: SensorEvent) {
        // Nos aseguramos de que el evento provenga del acelerómetro.
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {

            // Obtenemos la aceleración en los 3 ejes del espacio (X, Y, Z).
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Normalizamos los valores dividiendo por la gravedad terrestre (9.8 m/s²).
            // Esto nos da la fuerza en "G" (Fuerza G).
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            // Aplicamos el Teorema de Pitágoras en 3D para calcular la fuerza total del movimiento.
            // gForce = raíz cuadrada de (x² + y² + z²)
            val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

            // Si la fuerza detectada es mayor a 2.5G (un sacudón decente)...
            if (gForce > 2.5f) {
                val now = System.currentTimeMillis()

                // Comprobamos si han pasado al menos 1500 milisegundos desde el último "shake".
                if (now - lastShakeTime > 1500) {
                    lastShakeTime = now
                    // ¡EJECUTAMOS LA ACCIÓN! (En tu caso, buscar un artista aleatorio).
                    onShake()
                }
            }
        }
    }

    // Función obligatoria de la interfaz, aunque no solemos necesitarla para esto.
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}