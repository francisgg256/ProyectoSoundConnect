package com.example.firebase.presentation.initial

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
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
    viewModel: AuthViewModel? = null,
    navigateToLogin: () -> Unit = {},
    navigateToSignUp: () -> Unit = {},
    navigateToHome: () -> Unit = {}
) {
    val context = LocalContext.current

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                viewModel?.loginWithGoogle(
                    idToken = idToken,
                    onSuccess = { navigateToHome() },
                    onError = { Log.e("Google", "Error de Firebase") }
                )
            }
        } catch (e: ApiException) {
            Log.e("Google", "Fallo al conectar con Google. Código: ${e.statusCode}")
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Gray, Black), startY = 0f, endY = 600f))
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        // --- BOTÓN DE IDIOMA EN LA ESQUINA SUPERIOR DERECHA ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            val isEnglish = currentLocales.toLanguageTags().contains("en")

            Button(
                onClick = {
                    val newLocale = if (isEnglish) "es" else "en"
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLocale))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text(
                    text = if (isEnglish) "EN" else "ES",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logo de la app
        Image(
            painter = painterResource(R.drawable.spotify),
            contentDescription = "Logo",
            modifier = Modifier.clip(CircleShape).size(100.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Títulos de cabecera con el interlineado arreglado
        Text(
            text = stringResource(R.string.millions_songs),
            color = Color.White,
            fontSize = 38.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.free_on_app),
            color = Color.White,
            fontSize = 38.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { navigateToSignUp() },
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
        ) {
            Text(text = stringResource(R.string.register_free), color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        CustomButton(
            Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 32.dp)
                .background(BackgroundButton).border(2.dp, ShapeButton, CircleShape)
                .clickable { launcher.launch(googleSignInClient.signInIntent) },
            painterResource(R.drawable.google),
            stringResource(R.string.continue_google)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.login_text),
            color = Color.White,
            modifier = Modifier.padding(24.dp).clickable { navigateToLogin() },
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

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
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}