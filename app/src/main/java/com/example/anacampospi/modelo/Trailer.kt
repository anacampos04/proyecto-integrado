package com.example.anacampospi.modelo

data class Trailer(
    val key: String = "", // id de YouTube
    val thumbnailUrl: String = "", // https://img.youtube.com/vi/{key}/hqdefault.jpg
    val embedUrl: String = "" // https://www.youtube.com/embed/{key}?playsinline=1
)