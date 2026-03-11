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
import androidx.compose.runtime.*
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

// 1. PANTALLA ENVOLTORIO (STATE HOISTING)
@Composable
fun HomeScreen(viewmodel: HomeViewmodel) {
    val artists by viewmodel.artist.collectAsState()
    val player by viewmodel.player.collectAsState()
    val favorites by viewmodel.favorites.collectAsState()
    val profileImage by viewmodel.profileImage.collectAsState()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewmodel.updateProfileImage(bitmap)
        }
    }

    HomeScreenContent(
        artists = artists,
        favorites = favorites,
        player = player,
        profileImage = profileImage,
        onCameraClick = { cameraLauncher.launch(null) },
        onArtistClick = { viewmodel.addPlayer(it) },
        onPlayClick = { viewmodel.onPlaySelected() },
        onCancelClick = { viewmodel.onCancelSelected() },
        onSearch = { viewmodel.searchArtists(it) },
        onFavoriteClick = { viewmodel.onFavoriteClick(it) }
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
            if (profileImage != null) {
                Image(
                    bitmap = profileImage.asImageBitmap(),
                    contentDescription = stringResource(R.string.profile_pic_desc),
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onCameraClick() }
                )
            } else {
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
            
            // --- SALUDO (CON ARREGLO DE PESO) ---
            Text(
                text = stringResource(R.string.welcome_music_lover),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                // Al ponerle weight(1f), el texto se adapta al espacio sobrante
                // y evita empujar al botón fuera de la pantalla.
                modifier = Modifier.weight(1f)
            )

            // Margen de seguridad entre el texto y el botón
            Spacer(modifier = Modifier.width(8.dp))

            // --- BOTÓN MÁGICO DE IDIOMA ---
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            val isEnglish = currentLocales.toLanguageTags().contains("en")

            Button(
                onClick = {
                    val newLocale = if (isEnglish) "es" else "en"
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLocale))
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
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
                searchQuery = it
                onSearch(it)
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

        // --- LISTA DE RESULTADOS ---
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(artists) { artist ->
                val isFavorite = favorites.any { it.name.equals(artist.name, ignoreCase = true) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onArtistClick(artist) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    IconButton(onClick = { onFavoriteClick(artist) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.favorite_desc),
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }
            }
        }

        player?.let { PlayerComponent(it, onPlayClick, onCancelClick) }
    }
}

// 3. COMPONENTE DEL REPRODUCTOR
@Composable
fun PlayerComponent(player: Player, onPlaySelected: () -> Unit, onCancelSelected: () -> Unit) {
    val icon = if (player.play == true) R.drawable.ic_pause else R.drawable.ic_play

    Row(
        Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(Color(0xFF6650a4)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = player.artist?.name.orEmpty(),
            modifier = Modifier.padding(horizontal = 12.dp),
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.play_pause_desc),
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onPlaySelected)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = stringResource(R.string.close_desc),
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onCancelSelected)
        )
    }
}