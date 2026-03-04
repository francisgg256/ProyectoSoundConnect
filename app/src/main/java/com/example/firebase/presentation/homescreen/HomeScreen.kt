package com.example.firebase.presentation.homescreen

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.firebase.R
import com.example.firebase.data.model.Artist
import com.example.firebase.data.model.Player
import com.example.firebase.ui.theme.Purple40

@Composable
fun HomeScreen(viewmodel: HomeViewmodel) {
    val artists by viewmodel.artist.collectAsState()
    val player by viewmodel.player.collectAsState()

    HomeScreenContent(
        artists = artists,
        player = player,
        onArtistClick = { viewmodel.addPlayer(it) },
        onPlayClick = { viewmodel.onPlaySelected() },
        onCancelClick = { viewmodel.onCancelSelected() },
        onSearch = { viewmodel.searchArtists(it) }
    )
}

@Composable
fun HomeScreenContent(
    artists: List<Artist>,
    player: Player?,
    onArtistClick: (Artist) -> Unit,
    onPlayClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Barra de búsqueda
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

        // CUMPLIMOS REQUISITO: LazyColumn en lugar de LazyRow
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(artists) { artist ->
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
                        model = artist.image ?: "https://via.placeholder.com/150",
                        contentDescription = "Artist image"
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = artist.name.orEmpty(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp
                    )
                }
            }
        }
        
        player?.let { PlayerComponent(it, onPlayClick, onCancelClick) }
    }
}

@Composable
fun PlayerComponent(player: Player, onPlaySelected: () -> Unit, onCancelSelected: () -> Unit) {

    val icon = if (player.play == true) R.drawable.ic_pause else R.drawable.ic_play

    Row(
        Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(Purple40),
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
                .clickable(onClick = onPlaySelected)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = "Close",
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onCancelSelected)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val artistList = listOf(
        Artist("Artist 1", "Description 1", "https://via.placeholder.com/150"),
        Artist("Artist 2", "Description 2", "https://via.placeholder.com/150"),
        Artist("Artist 3", "Description 3", "https://via.placeholder.com/150")
    )
    val player = Player(artistList[0], play = true)
    HomeScreenContent(
        artists = artistList,
        player = player,
        onArtistClick = {},
        onPlayClick = {},
        onCancelClick = {},
        onSearch = {}
    )
}
