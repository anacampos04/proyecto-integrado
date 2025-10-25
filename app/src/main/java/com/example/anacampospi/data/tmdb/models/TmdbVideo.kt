package com.example.anacampospi.data.tmdb.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de respuesta de videos/trailers de TMDb
 */
data class TmdbVideo(
    @SerializedName("id")
    val id: String,

    @SerializedName("key")
    val key: String, // YouTube video key

    @SerializedName("name")
    val name: String,

    @SerializedName("site")
    val site: String, // YouTube, Vimeo, etc.

    @SerializedName("size")
    val size: Int, // 360, 480, 720, 1080

    @SerializedName("type")
    val type: String, // Trailer, Teaser, Clip, etc.

    @SerializedName("official")
    val official: Boolean? = false
) {
    /**
     * Construye la URL del thumbnail de YouTube
     */
    fun getYoutubeThumbnailUrl(): String {
        return if (site == "YouTube") {
            "https://img.youtube.com/vi/$key/hqdefault.jpg"
        } else {
            ""
        }
    }

    /**
     * Construye la URL del video en YouTube
     */
    fun getYoutubeUrl(): String {
        return if (site == "YouTube") {
            "https://www.youtube.com/watch?v=$key"
        } else {
            ""
        }
    }
}

/**
 * Respuesta de la API de videos
 */
data class TmdbVideosResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("results")
    val results: List<TmdbVideo>
)
