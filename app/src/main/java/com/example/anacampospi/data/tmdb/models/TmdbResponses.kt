package com.example.anacampospi.data.tmdb.models

import com.google.gson.annotations.SerializedName

/**
 * Respuesta paginada de películas de TMDb
 */
data class TmdbMovieResponse(
    @SerializedName("page")
    val page: Int,

    @SerializedName("results")
    val results: List<TmdbMovie>,

    @SerializedName("total_pages")
    val totalPages: Int,

    @SerializedName("total_results")
    val totalResults: Int
)

/**
 * Respuesta paginada de series de TV de TMDb
 */
data class TmdbTvResponse(
    @SerializedName("page")
    val page: Int,

    @SerializedName("results")
    val results: List<TmdbTvShow>,

    @SerializedName("total_pages")
    val totalPages: Int,

    @SerializedName("total_results")
    val totalResults: Int
)

/**
 * Respuesta de detalles de Watch Providers (plataformas de streaming)
 */
data class TmdbWatchProvidersResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("results")
    val results: Map<String, TmdbCountryProviders>
)

/**
 * Proveedores por país
 */
data class TmdbCountryProviders(
    @SerializedName("link")
    val link: String? = null,

    @SerializedName("flatrate")
    val flatrate: List<TmdbProvider>? = null,

    @SerializedName("buy")
    val buy: List<TmdbProvider>? = null,

    @SerializedName("rent")
    val rent: List<TmdbProvider>? = null
)

/**
 * Información de un proveedor individual
 */
data class TmdbProvider(
    @SerializedName("provider_id")
    val providerId: Int,

    @SerializedName("provider_name")
    val providerName: String,

    @SerializedName("logo_path")
    val logoPath: String? = null,

    @SerializedName("display_priority")
    val displayPriority: Int
)
