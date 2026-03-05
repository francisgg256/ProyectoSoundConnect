package com.example.firebase.presentation.login

// --- IMPORTACIONES NECESARIAS ---
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebase.R
import com.example.firebase.presentation.auth.AuthViewModel
import com.example.firebase.ui.theme.Black
import com.example.firebase.ui.theme.SelectedField
import com.example.firebase.ui.theme.UnselectedField

// Función "Inteligente": Se comunica con el ViewModel
@Composable
fun LoginScreen(viewModel: AuthViewModel, navigateToHome: () -> Unit, navigateBack: () -> Unit) {
    LoginContent(
        // Le pasamos la lógica al botón de login
        onLoginClick = { email, password ->
            viewModel.login(
                email = email,
                password = password,
                onSuccess = {
                    navigateToHome() // Si Firebase dice que los datos son correctos, vamos al Home
                    Log.i("Ignacio", "LOGIN OK")
                },
                onError = {
                    Log.i("Ignacio", "LOGIN KO") // Si falla (mala contraseña), da error en consola
                }
            )
        },
        navigateBack = navigateBack // Acción para la flecha de volver atrás
    )
}

// Función "Tonta": Solo dibuja la interfaz, no sabe nada de Firebase
@Composable
fun LoginContent(
    onLoginClick: (String, String) -> Unit,
    navigateBack: () -> Unit
) {
    // Variables de estado: Recuerdan lo que el usuario escribe en las cajas de texto
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black) // Fondo negro
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabecera con la flecha para volver atrás
        Row(){
            Icon(
                painter = painterResource(id = R.drawable.ic_back_24),
                contentDescription = "",
                tint = White,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(24.dp)
                    .clickable { navigateBack() } // Al tocarla, vuelve atrás
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // Campo de Email
        Text("Email", color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = email, // Muestra lo que hay guardado en la variable email
            onValueChange = { email = it }, // Actualiza la variable con cada nueva letra
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            )
        )

        Spacer(Modifier.height(48.dp))

        // Campo de Contraseña
        Text("Password", color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            )
        )

        Spacer(Modifier.height(48.dp))

        // Botón de iniciar sesión
        Button(onClick = { onLoginClick(email, password) }) {
            Text(text = "Login")
        }
    }
}

// Vista previa
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginContent(onLoginClick = { _, _ -> }, navigateBack = {})
}
