package com.example.anacampospi.data.tmdb.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de respuesta de una película de TMDb API
 */
data class TmdbMovie(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("original_title")
    val originalTitle: String? = null,

    @SerializedName("overview")
    val overview: String? = null,

    @SerializedName("poster_path")
    val posterPath: String? = null,

    @SerializedName("backdrop_path")
    val backdropPath: String? = null,

    @SerializedName("release_date")
    val releaseDate: String? = null,

    @SerializedName("vote_average")
    val voteAverage: Double? = 0.0,

    @SerializedName("vote_count")
    val voteCount: Int? = 0,

    @SerializedName("popularity")
    val popularity: Double? = 0.0,

    @SerializedName("genre_ids")
    val genreIds: List<Int>? = emptyList(),

    @SerializedName("adult")
    val adult: Boolean? = false,

    @SerializedName("video")
    val video: Boolean? = false,

    @SerializedName("original_language")
    val originalLanguage: String? = null
) {
    /**
     * Construye la URL completa del poster
     */
    fun getPosterUrl(size: String = "w500"): String {
        return if (posterPath != null) {
            "https://image.tmdb.org/t/p/$size$posterPath"
        } else {
            ""
        }
    }

    /**
     * Obtiene el año de estreno
     */
    fun getReleaseYear(): Int {
        return releaseDate?.take(4)?.toIntOrNull() ?: 0
    }
}
