package com.example.firebase.data.model

// Modelamos solo lo que necesitamos para cumplir con la UI de SoundConnect
data class MusicResponse(
    val results: ArtistResults
)
data class ArtistResults(
    val artistmatches: ArtistMatches
)
data class ArtistMatches(
    val artist: List<Artist>
)
