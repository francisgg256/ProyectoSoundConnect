import java.util.Properties

plugins {
    // 1. PLUGINS: Son herramientas que añaden funciones extra al proceso de compilación.
    alias(libs.plugins.android.application) // Indica que esto es una aplicación Android.
    alias(libs.plugins.kotlin.android)      // Habilita el lenguaje Kotlin.
    alias(libs.plugins.kotlin.compose)      // Habilita las herramientas de Jetpack Compose.
    alias(libs.plugins.googleServices)      // Necesario para conectar con Firebase.
    alias(libs.plugins.crashlytics)         // Para reportar errores críticos a la consola de Google.
    alias(libs.plugins.ksp)                 // Procesador moderno para generar código de Room (Base de datos).
}

// 2. SEGURIDAD DE LA API KEY
// Leemos el archivo 'local.properties' (que no se sube a GitHub) para extraer la clave de Google Maps.
// Esto es una buena práctica de seguridad profesional.
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""

android {
    namespace = "com.example.firebase"
    compileSdk = 36 // Versión del SDK con la que se compila la App.

    defaultConfig {
        applicationId = "com.example.firebase"
        minSdk = 24       // La App funcionará desde Android 7.0 en adelante.
        targetSdk = 36    // Versión de Android para la que se ha optimizado la App.
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inyectamos la API KEY leída arriba directamente en el Manifest.
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false // No ofuscamos el código para facilitar pruebas.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true // Activamos explícitamente Jetpack Compose.
    }
}

// 3. DEPENDENCIAS (LIBRERÍAS EXTERNAS)
dependencies {
    // FIREBASE: Usamos el BOM (Bill of Materials) para que todas las versiones de Firebase
    // (Auth, Database, etc.) sean compatibles entre sí automáticamente.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.realtime.database)

    // MULTIMEDIA Y NAVEGACIÓN
    implementation(libs.coil) // Para cargar imágenes de internet (Deezer).
    implementation(libs.androidx.navigation.compose) // Para movernos entre pantallas.

    // COMPOSE UI: Librerías para dibujar la interfaz.
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3) // Componentes visuales modernos.

    // PERSISTENCIA LOCAL (ROOM)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.appcompat)
    ksp(libs.androidx.room.compiler) // Procesador de la base de datos local.

    // GOOGLE MAPS Y LOCALIZACIÓN
    implementation("com.google.android.gms:play-services-auth:21.0.0") // Login con Google.
    implementation(libs.maps.compose)         // Mapas para Jetpack Compose.
    implementation(libs.play.services.maps)   // SDK base de Google Maps.
    implementation(libs.play.services.location) // Para obtener la ubicación GPS.

    // RED (RETROFIT): Para consumir la API de Deezer.
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson) // Convierte el JSON de la API en objetos de Kotlin.

    // TESTEO
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    // --- INTERNACIONALIZACIÓN (IDIOMAS) ---
    // Necesario para usar la API oficial de Google para el cambio de idioma dentro de la app (LocaleListCompat)
    implementation("androidx.appcompat:appcompat:1.6.1")
}
