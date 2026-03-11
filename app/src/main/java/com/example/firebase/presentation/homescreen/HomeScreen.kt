package com.example.firebase.presentation.homescreen

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate // Import para el idioma
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat // Import para la lista de idiomas
import coil.compose.AsyncImage
import com.example.firebase.R
import com.example.firebase.data.local.ArtistEntity
import com.example.firebase.data.model.Artist
import com.example.firebase.data.model.Player
import com.example.firebase.ui.theme.Purple40

// 1. PANTALLA ENVOLTORIO (STATE HOISTING)
// Esta primera función sirve solo para "observar" los datos del ViewModel y pasárselos a la pantalla visual.
@Composable
fun HomeScreen(viewmodel: HomeViewmodel) {
    // Escuchamos en tiempo real 4 cosas diferentes (StateFlows):
    val artists by viewmodel.artist.collectAsState() // Resultados de la búsqueda de Deezer
    val player by viewmodel.player.collectAsState()  // Lo que está sonando ahora mismo
    val favorites by viewmodel.favorites.collectAsState() // Favoritos guardados en Room
    val profileImage by viewmodel.profileImage.collectAsState() // Foto de perfil de la cámara

    // --- MAGIA DE LA CÁMARA (RA3) ---
    // Launcher que pide a Android abrir la app nativa de cámara.
    // TakePicturePreview() devuelve directamente un 'Bitmap' (miniatura) a la memoria RAM.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Si el usuario sacó la foto y la confirmó, la guardamos en el ViewModel
            viewmodel.updateProfileImage(bitmap)
        }
    }

    // Llamamos a la pantalla "física" real y le pasamos los datos y las funciones (eventos)
    HomeScreenContent(
        artists = artists,
        favorites = favorites,
        player = player,
        profileImage = profileImage,
        onCameraClick = { cameraLauncher.launch(null) }, // Al hacer clic en la foto, abre la cámara
        onArtistClick = { viewmodel.addPlayer(it) }, // Pone la canción a reproducirse
        onPlayClick = { viewmodel.onPlaySelected() }, // Botón Play/Pausa
        onCancelClick = { viewmodel.onCancelSelected() }, // Cierra el reproductor
        onSearch = { viewmodel.searchArtists(it) }, // Busca en internet
        onFavoriteClick = { viewmodel.onFavoriteClick(it) } // Añade/quita de Room
    )
}

// 2. LA INTERFAZ VISUAL REAL (UI)
@Composable
fun HomeScreenContent(
    artists: List<Artist>,
    favorites: List<ArtistEntity>,
    player: Player?,
    profileImage: Bitmap?,
    onCameraClick: () -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlayClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSearch: (String) -> Unit,
    onFavoriteClick: (Artist) -> Unit
) {
    // Variable local para guardar lo que el usuario teclea en la barra de búsqueda
    var searchQuery by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- CABECERA (FOTO DE PERFIL Y SALUDO) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Si hay una foto tomada con la cámara, la mostramos...
            if (profileImage != null) {
                Image(
                    bitmap = profileImage.asImageBitmap(), // Convierte el Bitmap normal a uno de Jetpack Compose
                    contentDescription = stringResource(R.string.profile_pic_desc),
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape) // La recorta en forma de círculo perfecto
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) // Borde dinámico
                        .clickable { onCameraClick() } // Si la tocas, puedes echarte otra foto
                )
            } else {
                // Si todavía no hay foto, mostramos un icono gris genérico
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.add_photo_desc),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.3f))
                        .clickable { onCameraClick() }
                        .padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.welcome_music_lover),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // --- BOTÓN MÁGICO DE IDIOMA ---
            Spacer(modifier = Modifier.weight(1f)) // Empuja el botón a la derecha del todo

            // Obtenemos el idioma actual que tiene seleccionado la app
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            val isEnglish = currentLocales.toLanguageTags().contains("en")

            Button(
                onClick = {
                    // Si estamos en inglés, pasamos a español, y viceversa.
                    val newLocale = if (isEnglish) "es" else "en"
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLocale))
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                // Texto dinámico: si está en inglés, el botón dice "EN", sino "ES"
                Text(
                    text = if (isEnglish) "EN" else "ES",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- BARRA DE BÚSQUEDA ---
        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it // Actualiza el texto en pantalla
                onSearch(it) // Ejecuta la llamada a Retrofit/Deezer al instante
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.DarkGray,
                unfocusedContainerColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // --- TÍTULO DE LISTA ---
        Text(
            text = stringResource(R.string.search_results),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // --- LISTA DE RESULTADOS DE LA API (LazyColumn) ---
        // Usamos modifier.weight(1f) para que ocupe todo el espacio central sobrante
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(artists) { artist ->

                // COMPROBACIÓN DE FAVORITOS (Reactivo)
                // Mira si el artista actual que ha llegado de internet, ya está en nuestra BD local
                val isFavorite = favorites.any { it.name.equals(artist.name, ignoreCase = true) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onArtistClick(artist) } // Al tocar la fila, se pone a reproducir
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AsyncImage (Coil): Descarga asíncronamente la foto del artista desde la URL
                    AsyncImage(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        model = artist.image?.lastOrNull()?.url ?: "https://via.placeholder.com/150",
                        contentDescription = stringResource(R.string.artist_image_desc)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = artist.name.orEmpty(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // BOTÓN DE FAVORITO (Corazón)
                    IconButton(onClick = { onFavoriteClick(artist) }) {
                        Icon(
                            // Si isFavorite es true pone el corazón relleno, si no, solo el contorno
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.favorite_desc),
                            // Si isFavorite es true lo pinta de rojo
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }
            }
        }

        // --- REPRODUCTOR (BARRA INFERIOR) ---
        // El bloque 'let' hace que esto SOLO se dibuje en pantalla si hay una canción sonando (si player no es nulo)
        player?.let { PlayerComponent(it, onPlayClick, onCancelClick) }
    }
}

// 3. COMPONENTE DEL REPRODUCTOR (El rectángulo inferior)
@Composable
fun PlayerComponent(player: Player, onPlaySelected: () -> Unit, onCancelSelected: () -> Unit) {

    // Decide qué icono mostrar (Play o Pausa) dependiendo del estado que manda Firebase
    val icon = if (player.play == true) R.drawable.ic_pause else R.drawable.ic_play

    Row(
        Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(Purple40), // Usa el color morado de nuestro tema
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nombre de la canción
        Text(
            text = player.artist?.name.orEmpty(),
            modifier = Modifier.padding(horizontal = 12.dp),
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f)) // Empuja los botones hacia la derecha del todo

        // Botón Play/Pausa
        Image(
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.play_pause_desc),
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onPlaySelected)
        )
        // Botón Cerrar (X)
        Image(
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = stringResource(R.string.close_desc),
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onCancelSelected)
        )
    }
}
