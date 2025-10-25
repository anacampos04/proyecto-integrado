package com.example.anacampospi.data.tmdb.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de respuesta de una serie de TV de TMDb API
 */
data class TmdbTvShow(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("original_name")
    val originalName: String? = null,

    @SerializedName("overview")
    val overview: String? = null,

    @SerializedName("poster_path")
    val posterPath: String? = null,

    @SerializedName("backdrop_path")
    val backdropPath: String? = null,

    @SerializedName("first_air_date")
    val firstAirDate: String? = null,

    @SerializedName("vote_average")
    val voteAverage: Double? = 0.0,

    @SerializedName("vote_count")
    val voteCount: Int? = 0,

    @SerializedName("popularity")
    val popularity: Double? = 0.0,

    @SerializedName("genre_ids")
    val genreIds: List<Int>? = emptyList(),

    @SerializedName("origin_country")
    val originCountry: List<String>? = emptyList(),

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
    fun getFirstAirYear(): Int {
        return firstAirDate?.take(4)?.toIntOrNull() ?: 0
    }
}
