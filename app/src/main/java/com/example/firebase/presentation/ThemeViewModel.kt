package com.example.firebase.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

// El ViewModel implementa SensorEventListener para escuchar al sensor de luz directamente.
class ThemeViewModel : ViewModel(), SensorEventListener {

    // Estado que indica si debemos aplicar el tema oscuro.
    // Es un 'mutableStateOf' de Compose para que, al cambiar, TODA la app se repinte al instante.
    private val _isDarkTheme = mutableStateOf(false)
    val isDarkTheme: State<Boolean> = _isDarkTheme

    // Umbral de oscuridad (10 luxes).
    // Si hay menos de 10 luxes (estás a oscuras), activamos el modo noche.
    private val DARK_THRESHOLD = 10f

    // Función para empezar a escuchar al sensor. Se suele llamar desde el MainActivity.
    fun startListening(sensorManager: SensorManager) {
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        lightSensor?.let {
            // Registramos el listener con un retraso NORMAL (suficiente para cambios de luz).
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // Es vital dejar de escuchar al cerrar la app para no gastar batería.
    fun stopListening(sensorManager: SensorManager) {
        sensorManager.unregisterListener(this)
    }

    // Esta función salta cada vez que cambia la luz ambiental (ej: entras en un túnel o apagas la luz).
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            // event.values[0] contiene el nivel de luz en luxes.
            val lightLevel = event.values[0]

            // Actualizamos el estado: si la luz es menor al umbral, isDarkTheme será true.
            _isDarkTheme.value = lightLevel < DARK_THRESHOLD
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}