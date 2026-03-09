package com.example.firebase

import android.app.Application
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

// Heredar de Application() convierte a esta clase en la "madre" de todas tus pantallas.
// Implementar OnMapsSdkInitializedCallback nos permite recibir un aviso cuando los Mapas estén listos.
class SoundConnectApp : Application(), OnMapsSdkInitializedCallback {

    // onCreate en la clase Application se ejecuta una sola vez al arrancar la App.
    override fun onCreate() {
        super.onCreate()

        // --- INICIALIZACIÓN DE GOOGLE MAPS ---
        // Forzamos el uso del motor de renderizado "LATEST" (el más nuevo).
        // Esto evita errores visuales y mejora el rendimiento de los mapas en dispositivos modernos.
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
    }

    // Este método se dispara automáticamente cuando el SDK de Mapas termina de cargar.
    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            // Logueamos en la consola si estamos usando la versión moderna o la antigua (Legacy).
            MapsInitializer.Renderer.LATEST -> Log.d("MapsInitializer", "Se está usando la última versión del renderizador.")
            MapsInitializer.Renderer.LEGACY -> Log.d("MapsInitializer", "Se está usando la versión antigua del renderizador.")
        }
    }
}
