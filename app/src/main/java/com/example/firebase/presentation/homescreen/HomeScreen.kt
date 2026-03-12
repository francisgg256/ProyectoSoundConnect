package com.example.firebase.presentation.homescreen

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
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
import androidx.core.os.LocaleListCompat
import coil.compose.AsyncImage
import com.example.firebase.R
import com.example.firebase.data.local.ArtistEntity
import com.example.firebase.data.model.Artist
import com.example.firebase.data.model.Player

@Composable
fun HomeScreen(viewmodel: HomeViewmodel, onLogoutClick: () -> Unit) {
    val artists by viewmodel.artist.collectAsState()
    val player by viewmodel.player.collectAsState()
    val favorites by viewmodel.favorites.collectAsState()
    val profileImage by viewmodel.profileImage.collectAsState()
    val userName by viewmodel.userName.collectAsState()

    LaunchedEffect(Unit) {
        viewmodel.reloadCurrentUser()
    }

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
        userName = userName,
        onLogoutClick = onLogoutClick,
        onNameChange = { viewmodel.updateUserName(it) },
        onCameraClick = { cameraLauncher.launch(null) },
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
    userName: String,
    onLogoutClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onCameraClick: () -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlayClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSearch: (String) -> Unit,
    onFavoriteClick: (Artist) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showNameDialog by remember { mutableStateOf(false) }
    var newNameText by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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

            Text(
                text = "¡Hola, $userName!",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .clickable { showNameDialog = true }
            )

            Spacer(modifier = Modifier.width(8.dp))

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

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { onLogoutClick() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Cerrar Sesión",
                    tint = Color.Red
                )
            }
        }

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

        Text(
            text = stringResource(R.string.search_results),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

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
                        model = artist.imageUrl ?: "https://via.placeholder.com/150",
                        contentDescription = stringResource(R.string.artist_image_desc)
                    )
                    Spacer(modifier = Modifier.size(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = artist.name.orEmpty(),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp
                        )

                        artist.listeners?.let { oyentes ->
                            Text(
                                text = "$oyentes fans",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

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

        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("¿Cómo te llamas?", fontWeight = FontWeight.Bold) },
                text = {
                    TextField(
                        value = newNameText,
                        onValueChange = { newNameText = it },
                        placeholder = { Text("Escribe tu nombre") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        onNameChange(newNameText)
                        showNameDialog = false
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    Button(onClick = { showNameDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}


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