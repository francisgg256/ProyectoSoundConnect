package com.example.firebase.presentation.login

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState // Necesario para el modo horizontal
import androidx.compose.foundation.verticalScroll   // Necesario para el modo horizontal
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebase.R
import com.example.firebase.presentation.auth.AuthViewModel
import com.example.firebase.ui.theme.Black
import com.example.firebase.ui.theme.SelectedField
import com.example.firebase.ui.theme.UnselectedField

// --- 1. FUNCIÓN ENVOLTORIO (STATEFUL) ---
// Esta función es la que interactúa con la lógica de negocio (el ViewModel).
// NO dibuja nada directamente, solo conecta la lógica con la interfaz gráfica.
@Composable
fun LoginScreen(viewModel: AuthViewModel, navigateToHome: () -> Unit, navigateBack: () -> Unit) {
    // Obtenemos el contexto de Android para poder mostrar mensajes flotantes (Toasts)
    val context = LocalContext.current

    // Llamamos a la función que dibuja la pantalla (LoginContent) y le pasamos los eventos
    LoginContent(
        // Cuando el usuario le dé al botón de Login en la pantalla gráfica, ejecutamos esto:
        onLoginClick = { email, password ->
            // Le pedimos al ViewModel que hable con Firebase
            viewModel.login(
                email = email,
                password = password,
                onSuccess = {
                    navigateToHome() // Si Firebase dice OK, navegamos al Home
                    Log.i("Ignacio", "LOGIN OK") // Lo apuntamos en consola (ideal para depurar)
                },
                onError = {
                    Log.i("Ignacio", "LOGIN KO")
                    // Si Firebase falla (contraseña mal), mostramos un Toast en la pantalla
                    Toast.makeText(context, context.getString(R.string.login_error), Toast.LENGTH_SHORT).show()
                }
            )
        },
        navigateBack = navigateBack // Pasamos la función de ir atrás al botón superior
    )
}

// --- 2. FUNCIÓN GRÁFICA (STATELESS / PURA) ---
// Esta función SÓLO dibuja botones y cajas de texto.
@Composable
fun LoginContent(
    onLoginClick: (String, String) -> Unit, // Función Lambda: "Avisaré cuando pulsen el botón y mandaré 2 textos"
    navigateBack: () -> Unit
) {
    // Variables locales para guardar lo que el usuario va escribiendo
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Estado del scroll para que la pantalla sea cómoda en horizontal
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black) // Fondo negro
            .padding(horizontal = 32.dp)
            .verticalScroll(scrollState), // Habilita el scroll si el contenido no cabe (ej: modo horizontal)
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- BOTÓN DE ATRÁS ---
        Row(){
            Icon(
                painter = painterResource(id = R.drawable.ic_back_24),
                contentDescription = "",
                tint = White,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(24.dp)
                    .clickable { navigateBack() } // Dispara el evento de volver a la pantalla inicial
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // --- CAJA DE TEXTO DEL EMAIL ---
        Text(stringResource(R.string.email_label), color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = email,
            onValueChange = { email = it }, // Actualiza la variable con cada tecla pulsada
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            )
        )

        Spacer(Modifier.height(48.dp))

        // --- CAJA DE TEXTO DE LA CONTRASEÑA ---
        Text(stringResource(R.string.password_label), color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            )
        )

        // Espacio antes del botón
        Spacer(Modifier.height(48.dp))

        // --- BOTÓN DE INICIO DE SESIÓN ---
        Button(
            onClick = {
                // Dispara la función lambda hacia arriba, mandando el email y password actuales
                onLoginClick(email, password)
            },
            modifier = Modifier.padding(bottom = 32.dp) // Margen inferior para que no quede pegado al borde en horizontal
        ) {
            Text(text = stringResource(R.string.login_button))
        }
    }
}
