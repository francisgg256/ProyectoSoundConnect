package com.example.firebase.presentation.initial

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebase.R
import com.example.firebase.presentation.auth.AuthViewModel
import com.example.firebase.ui.theme.BackgroundButton
import com.example.firebase.ui.theme.Black
import com.example.firebase.ui.theme.Gray
import com.example.firebase.ui.theme.ShapeButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun InitialScreen(
    // Recibe el ViewModel de autenticación (por defecto nulo para las vistas previas)
    viewModel: AuthViewModel? = null,
    // Recibe FUNCIONES LAMBDA para la navegación.
    // La pantalla no sabe cómo navegar, solo "avisa" de que el usuario quiere ir a un sitio.
    navigateToLogin: () -> Unit = {},
    navigateToSignUp: () -> Unit = {},
    navigateToHome: () -> Unit = {}
) {
    // Obtenemos el contexto actual (necesario para las herramientas de Google)
    val context = LocalContext.current

    // --- 1. CONFIGURACIÓN DEL CLIENTE DE GOOGLE ---
    // Usamos 'remember' para que esta configuración no se vuelva a crear si la pantalla gira.
    val googleSignInClient = remember {
        // Configuramos qué le vamos a pedir a Google:
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Le pedimos el 'Token ID' (la llave secreta) usando el ID de cliente web de Firebase
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail() // También le pedimos permiso para ver su email
            .build()

        // Creamos el cliente oficial de Google con esta configuración
        GoogleSignIn.getClient(context, gso)
    }

    // --- 2. EL LANZADOR DE LA VENTANA DE GOOGLE ---
    // Igual que con la cámara, usamos este 'Launcher' moderno en lugar de startActivityForResult
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Este bloque se ejecuta cuando el usuario selecciona su cuenta de Gmail en la ventanita emergente
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            // Intentamos extraer la cuenta de Google
            val account = task.getResult(ApiException::class.java)

            // Si todo va bien y nos da el idToken...
            account?.idToken?.let { idToken ->
                // ¡AQUÍ CONECTAMOS GOOGLE CON FIREBASE!
                // Le pasamos la llave de Google a nuestro ViewModel para que inicie sesión en la base de datos
                viewModel?.loginWithGoogle(
                    idToken = idToken,
                    onSuccess = { navigateToHome() }, // Si Firebase lo acepta, nos vamos al Home
                    onError = { Log.e("Google", "Error de Firebase") }
                )
            }
        } catch (e: ApiException) {
            // Si el usuario cierra la ventanita sin elegir cuenta o no hay internet, capturamos el error
            Log.e("Google", "Fallo al conectar con Google. Código: ${e.statusCode}")
        }
    }

    // --- 3. INTERFAZ GRÁFICA (UI) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Dibuja un fondo con un degradado vertical (de Gris a Negro)
            .background(Brush.verticalGradient(listOf(Gray, Black), startY = 0f, endY = 600f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f)) // Los weight(1f) actúan como muelles, empujando el contenido al centro

        // Logo de la app (usando la imagen 'spotify' de tus recursos)
        Image(
            painter = painterResource(R.drawable.spotify),
            contentDescription = "Logo",
            modifier = Modifier.clip(CircleShape).size(100.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Textos promocionales
        Text("Millones de canciones.", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)
        Text("Gratis en SoundConnect", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.weight(1f))

        // --- BOTÓN DE REGISTRO ---
        Button(
            onClick = { navigateToSignUp() }, // Llama a la función lambda de arriba
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            Text(text = "Regístrate gratis", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- BOTÓN DE GOOGLE (Componente Personalizado) ---
        CustomButton(
            Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 32.dp)
                .background(BackgroundButton).border(2.dp, ShapeButton, CircleShape)
                .clickable {
                    // ¡AL HACER CLIC, ABRIMOS LA VENTANA DE SELECCIÓN DE CUENTA DE GOOGLE!
                    launcher.launch(googleSignInClient.signInIntent)
                },
            painterResource(R.drawable.google), // Le pasamos el icono de la G
            "Continuar con Google"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- TEXTO PARA INICIAR SESIÓN (EMAIL/CLAVE) ---
        Text(
            text = "Iniciar Sesión",
            color = Color.White,
            modifier = Modifier.padding(24.dp).clickable { navigateToLogin() },
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

// --- 4. COMPONENTE REUTILIZABLE (CUSTOM BUTTON) ---
// Extraemos este botón a una función aparte para mantener el código limpio y poder reutilizarlo si queremos
@Composable
fun CustomButton(modifier: Modifier, painter: Painter, title: String) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        Image(
            painter = painter,
            contentDescription = "",
            modifier = Modifier.padding(start = 16.dp).size(16.dp)
        )
        Text(
            text = title,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center, // Centramos el texto ignorando el icono de la izquierda
            fontWeight = FontWeight.Bold
        )
    }
}
