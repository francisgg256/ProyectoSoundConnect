package com.example.firebase

import android.app.Application
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

class SoundConnectApp : Application(), OnMapsSdkInitializedCallback {

    override fun onCreate() {
        super.onCreate()

        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
    }

    // Este método se dispara automáticamente cuando el SDK de Mapas termina de cargar.
    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d("MapsInitializer", "Se está usando la última versión del renderizador.")
            MapsInitializer.Renderer.LEGACY -> Log.d("MapsInitializer", "Se está usando la versión antigua del renderizador.")
        }
    }
}
