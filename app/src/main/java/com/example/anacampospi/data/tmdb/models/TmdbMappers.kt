package com.example.anacampospi.data.tmdb.models

import com.example.anacampospi.modelo.ContenidoLite
import com.example.anacampospi.modelo.Trailer
import com.example.anacampospi.modelo.enums.TipoContenido

/**
 * Extensiones para mapear modelos de TMDb a ContenidoLite
 */

/**
 * Convierte TmdbMovie a ContenidoLite
 */
fun TmdbMovie.toContenidoLite(
    proveedores: List<String> = emptyList(),
    trailerKey: String? = null
): ContenidoLite {
    return ContenidoLite(
        idContenido = "movie:$id",
        titulo = title,
        posterUrl = getPosterUrl(),
        tipo = TipoContenido.PELICULA,
        anioEstreno = getReleaseYear(),
        proveedores = proveedores,
        puntuacion = voteAverage ?: 0.0,
        sinopsis = overview,
        generos = TmdbGenres.getGenreNames(genreIds),
        trailer = trailerKey?.let {
            Trailer(
                key = it,
                thumbnailUrl = "https://img.youtube.com/vi/$it/hqdefault.jpg",
                embedUrl = "https://www.youtube.com/embed/$it?playsinline=1"
            )
        }
    )
}

/**
 * Convierte TmdbTvShow a ContenidoLite
 */
fun TmdbTvShow.toContenidoLite(
    proveedores: List<String> = emptyList(),
    trailerKey: String? = null
): ContenidoLite {
    return ContenidoLite(
        idContenido = "tv:$id",
        titulo = name,
        posterUrl = getPosterUrl(),
        tipo = TipoContenido.SERIE,
        anioEstreno = getFirstAirYear(),
        proveedores = proveedores,
        puntuacion = voteAverage ?: 0.0,
        sinopsis = overview,
        generos = TmdbGenres.getGenreNames(genreIds),
        trailer = trailerKey?.let {
            Trailer(
                key = it,
                thumbnailUrl = "https://img.youtube.com/vi/$it/hqdefault.jpg",
                embedUrl = "https://www.youtube.com/embed/$it?playsinline=1"
            )
        }
    )
}

/**
 * Extrae los IDs de proveedores disponibles en streaming (flatrate)
 */
fun TmdbCountryProviders.getProviderIds(): List<String> {
    return flatrate?.map { it.providerId.toString() } ?: emptyList()
}
