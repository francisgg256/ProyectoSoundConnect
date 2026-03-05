package com.example.firebase.data.model

data class ItunesResponse(
    val results: List<ItunesTrack>
)

data class ItunesTrack(
    val previewUrl: String?
)