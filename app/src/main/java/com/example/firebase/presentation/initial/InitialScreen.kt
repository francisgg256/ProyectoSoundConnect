package com.example.firebase.presentation.initial

// --- IMPORTACIONES NECESARIAS ---
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebase.R
import com.example.firebase.ui.theme.BackgroundButton
import com.example.firebase.ui.theme.Black
import com.example.firebase.ui.theme.Gray
import com.example.firebase.ui.theme.ShapeButton

// Función principal de la pantalla. Recibe "acciones" que se ejecutarán cuando se pulse un botón.
@Composable
fun InitialScreen(
    navigateToLogin: () -> Unit = {},
    navigateToSignUp: () -> Unit = {},
    onGoogleLoginClick: () -> Unit = {},
    onFacebookLoginClick: () -> Unit = {}
) {
    // Column apila los elementos uno debajo de otro.
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Dibuja un fondo con degradado (Brush) que va desde el color Gray al Black.
            .background(Brush.verticalGradient(listOf(Gray, Black), startY = 0f, endY = 600f)),
        horizontalAlignment = Alignment.CenterHorizontally // Centra todo horizontalmente
    ) {
        // Spacer con weight(1f) actúa como un "muelle" invisible que empuja los elementos para distribuirlos.
        Spacer(modifier = Modifier.weight(1f))

        // Logo de la app (en este caso el icono de Spotify)
        Image(
            painter = painterResource(R.drawable.spotify),
            contentDescription = "Logo",
            modifier = Modifier.clip(CircleShape).size(100.dp) // Lo recorta en forma de círculo
        )

        Spacer(modifier = Modifier.weight(1f))

        // Textos de bienvenida
        Text("Millones de canciones.", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)
        Text("Gratis en SoundConnect", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.weight(1f))

        // Botón principal verde para registrarse
        Button(
            onClick = { navigateToSignUp() }, // Al hacer clic, navega a la pantalla de registro
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            Text(text = "Regístrate gratis", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Usamos nuestro componente personalizado para el botón de Google
        CustomButton(
            Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 32.dp)
                .background(BackgroundButton).border(2.dp, ShapeButton, CircleShape)
                .clickable { onGoogleLoginClick() },
            painterResource(R.drawable.google),
            "Continuar con Google"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Usamos nuestro componente personalizado para el botón de Facebook
        CustomButton(
            Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 32.dp)
                .background(BackgroundButton).border(2.dp, ShapeButton, CircleShape)
                .clickable { onFacebookLoginClick() },
            painterResource(R.drawable.facebook),
            "Continuar con Facebook"
        )

        // Texto inferior para los que ya tienen cuenta (les lleva al Login)
        Text(
            text = "Iniciar Sesión",
            color = Color.White,
            modifier = Modifier.padding(24.dp).clickable { navigateToLogin() },
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

// COMPONENTE REUTILIZABLE: Creamos una "plantilla" de botón para no tener que repetir código
// con el botón de Google y el de Facebook.
@Composable
fun CustomButton(modifier: Modifier, painter: Painter, title: String) {
    // Box superpone elementos uno encima del otro
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        // El icono de la red social a la izquierda
        Image(
            painter = painter,
            contentDescription = "",
            modifier = Modifier.padding(start = 16.dp).size(16.dp)
        )
        // El texto centrado ocupando todo el ancho
        Text(
            text = title,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

// Función que nos permite ver la vista previa (Preview) en el lado derecho de Android Studio
@Preview(showBackground = true)
@Composable
fun InitialScreenPreview() {
    InitialScreen()
}
