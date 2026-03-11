package com.example.firebase.presentation.signup

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    navigateToHome: () -> Unit,
    navigateBack: () -> Unit
) {
    // Obtenemos el contexto para poder lanzar el Toast en caso de error
    val context = LocalContext.current

    // ESTADOS LOCALES: Guardan lo que el usuario escribe antes de darle al botón
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Estado para permitir scroll en modo horizontal
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black) // Fondo temático oscuro
            .padding(horizontal = 32.dp)
            .verticalScroll(scrollState), // Habilitamos scroll vertical
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- BOTÓN VOLVER ---
        Row(){
            Icon(
                painter = painterResource(id = R.drawable.ic_back_24),
                contentDescription = "Volver",
                tint = White,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(24.dp)
                    .clickable { navigateBack() } // Ejecuta el lambda de navegación hacia atrás
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // --- CAMPO EMAIL ---
        Text(stringResource(R.string.email_label), color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField,
                unfocusedTextColor = White,
                focusedTextColor = White
            )
        )

        Spacer(Modifier.height(48.dp))

        // --- CAMPO PASSWORD ---
        Text(stringResource(R.string.password_label), color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField,
                unfocusedTextColor = White,
                focusedTextColor = White
            )
        )

        Spacer(Modifier.height(48.dp))

        // --- BOTÓN DE REGISTRO ---
        Button(
            onClick = {
                // Llamamos a la lógica de registro del ViewModel
                viewModel.signUp(
                    email = email,
                    password = password,
                    onSuccess = {
                        // Si Firebase crea el usuario con éxito, navegamos al Home
                        Log.i("Francisco", "Registro OK")
                        navigateToHome()
                    },
                    onError = {
                        // Si hay error, avisamos al usuario
                        Log.i("Francisco", "Registro KO")
                        Toast.makeText(context, context.getString(R.string.signup_error), Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(text = stringResource(R.string.signup_button))
        }
    }
}
