package com.example.firebase.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

// Hereda de ViewModel y también implementa "SensorEventListener" (se suscribe para escuchar un sensor).
class ThemeViewModel : ViewModel(), SensorEventListener {

    // Variable que guarda si el modo oscuro está activado (true) o no (false).
    private val _isDarkTheme = mutableStateOf(false)
    val isDarkTheme: State<Boolean> = _isDarkTheme

    // El límite. Si la luz de la habitación baja de 10 (casi a oscuras), se activará el modo oscuro.
    private val DARK_THRESHOLD = 10f

    // Función para "encender" el sensor cuando abrimos la app.
    fun startListening(sensorManager: SensorManager) {
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // Función para "apagar" el sensor cuando cerramos la app (para no gastar batería).
    fun stopListening(sensorManager: SensorManager) {
        sensorManager.unregisterListener(this)
    }

    // Esta función se dispara SOLA cada vez que la luz de la habitación cambia.
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightLevel = event.values[0] // Cogemos el valor de la luz actual.
            // Si la luz es menor que 10, _isDarkTheme se vuelve "true" (modo oscuro).
            _isDarkTheme.value = lightLevel < DARK_THRESHOLD
        }
    }

    // Función obligatoria del sensor, aunque no la usemos.
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}