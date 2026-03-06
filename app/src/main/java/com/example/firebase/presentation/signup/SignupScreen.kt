package com.example.firebase.presentation.signup

// --- IMPORTACIONES NECESARIAS ---
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebase.R
import com.example.firebase.presentation.auth.AuthViewModel
import com.example.firebase.ui.theme.Black
import com.example.firebase.ui.theme.SelectedField
import com.example.firebase.ui.theme.UnselectedField

@Composable
fun SignupScreen(viewModel: AuthViewModel, navigateToHome: () -> Unit, navigateBack: () -> Unit) {
    
    // Obtenemos el contexto para el Toast
    val context = LocalContext.current
    
    // Variables para guardar el email y contraseña que el usuario quiere crear
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Flecha hacia atrás
        Row(){
            Icon(
                painter = painterResource(id = R.drawable.ic_back_24),
                contentDescription = "",
                tint = White,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(24.dp)
                    .clickable { navigateBack() } // Vuelve a la pantalla de bienvenida
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // Campo para escribir el email
        Text("Email", color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            )
        )

        Spacer(Modifier.height(48.dp))

        // Campo para escribir la contraseña
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

        // Botón principal de Registro
        Button(onClick = {
            // Llama a la función signUp del AuthViewModel en lugar de login
            viewModel.signUp(
                email = email,
                password = password,
                onSuccess = {
                    Log.i("Francisco", "Registro OK") // Informa por consola
                    navigateToHome() // Si Firebase lo crea bien, lleva al usuario al mapa/música
                },
                onError = {
                    Log.i("Francisco", "Registro KO")
                    // Mostramos el mensaje si el correo ya existe o la contraseña es muy corta
                    Toast.makeText(context, "Error al registrarse. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show()
                }
            )
        }) {
            Text(text = "Sign Up") // Sign Up = Registrarse
        }
    }
}
