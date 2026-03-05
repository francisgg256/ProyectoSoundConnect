package com.example.firebase.presentation.homescreen

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.firebase.R
import com.example.firebase.data.local.ArtistEntity
import com.example.firebase.data.model.Artist
import com.example.firebase.data.model.Player
import com.example.firebase.ui.theme.Purple40

@Composable
fun HomeScreen(viewmodel: HomeViewmodel) {
    // Escuchamos las variables del ViewModel
    val artists by viewmodel.artist.collectAsState()
    val player by viewmodel.player.collectAsState()
    val favorites by viewmodel.favorites.collectAsState()
    val profileImage by viewmodel.profileImage.collectAsState()

    // Preparador de la cámara del dispositivo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewmodel.updateProfileImage(bitmap) // Guarda la foto en el ViewModel
        }
    }

    HomeScreenContent(
        artists = artists,
        favorites = favorites,
        player = player,
        profileImage = profileImage,
        onCameraClick = { cameraLauncher.launch(null) }, // Al hacer clic, abre cámara
        onArtistClick = { viewmodel.addPlayer(it) },
        onPlayClick = { viewmodel.onPlaySelected() },
        onCancelClick = { viewmodel.onCancelSelected() },
        onSearch = { viewmodel.searchArtists(it) },
        onFavoriteClick = { viewmodel.onFavoriteClick(it) }
    )
}

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
    var searchQuery by remember { mutableStateOf("") } // Recuerda el texto escrito

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- CABECERA DE PERFIL ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dibuja la foto redonda si existe, si no un icono por defecto
            if (profileImage != null) {
                Image(
                    bitmap = profileImage.asImageBitmap(),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onCameraClick() }
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Añadir foto",
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
                text = "¡Hola, amante de la música!",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // --- CAJA DE BÚSQUEDA ---
        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearch(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar artista en internet...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.DarkGray,
                unfocusedContainerColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Text(
            text = "Resultados de búsqueda",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // --- LISTA DE ARTISTAS ---
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(artists) { artist ->
                // Comprueba si este artista en concreto está en favoritos
                val isFavorite = favorites.any { it.name.equals(artist.name, ignoreCase = true) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onArtistClick(artist) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dibuja la foto del artista desde internet
                    AsyncImage(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        model = artist.image?.lastOrNull()?.url ?: "https://via.placeholder.com/150",
                        contentDescription = "Artist image"
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = artist.name.orEmpty(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // Dibuja el corazón
                    IconButton(onClick = { onFavoriteClick(artist) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }
            }
        }

        // --- REPRODUCTOR ---
        // Solo dibuja el reproductor morado si 'player' no es null
        player?.let { PlayerComponent(it, onPlayClick, onCancelClick) }
    }
}

@Composable
fun PlayerComponent(player: Player, onPlaySelected: () -> Unit, onCancelSelected: () -> Unit) {

    // Cambia el icono dependiendo de si la música está en Play o Pause
    val icon = if (player.play == true) R.drawable.ic_pause else R.drawable.ic_play

    Row(
        Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(Purple40), // Barra morada
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
            contentDescription = "play/pause",
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onPlaySelected) // Acción de pausar/reanudar
        )
        Image(
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = "Close",
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onCancelSelected) // Acción de cerrar
        )
    }
}
