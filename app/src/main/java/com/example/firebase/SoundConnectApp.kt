package com.example.firebase

import android.app.Application
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

class SoundConnectApp : Application(), OnMapsSdkInitializedCallback {
    override fun onCreate() {
        super.onCreate()
        // Initialize Maps SDK with the latest renderer to avoid some legacy database issues
        // like "Database lock unavailable"
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d("MapsInitializer", "The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY -> Log.d("MapsInitializer", "The legacy version of the renderer is used.")
        }
    }
}
