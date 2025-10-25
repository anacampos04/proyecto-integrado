package com.example.anacampospi.data.tmdb.models

/**
 * Catálogo de géneros de TMDb con sus IDs y nombres en español
 */
object TmdbGenres {

    // Géneros de películas
    private val movieGenres = mapOf(
        28 to "Acción",
        12 to "Aventura",
        16 to "Animación",
        35 to "Comedia",
        80 to "Crimen",
        99 to "Documental",
        18 to "Drama",
        10751 to "Familiar",
        14 to "Fantasía",
        36 to "Historia",
        27 to "Terror",
        10402 to "Música",
        9648 to "Misterio",
        10749 to "Romance",
        878 to "Ciencia ficción",
        10770 to "Película de TV",
        53 to "Suspense",
        10752 to "Bélica",
        37 to "Western"
    )

    // Géneros de series
    private val tvGenres = mapOf(
        10759 to "Acción y Aventura",
        16 to "Animación",
        35 to "Comedia",
        80 to "Crimen",
        99 to "Documental",
        18 to "Drama",
        10751 to "Familiar",
        10762 to "Infantil",
        9648 to "Misterio",
        10763 to "Noticias",
        10764 to "Reality",
        10765 to "Sci-Fi y Fantasía",
        10766 to "Telenovela",
        10767 to "Talk Show",
        10768 to "Guerra y Política",
        37 to "Western"
    )

    /**
     * Obtiene el nombre de un género por su ID
     */
    fun getGenreName(genreId: Int): String? {
        return movieGenres[genreId] ?: tvGenres[genreId]
    }

    /**
     * Convierte una lista de IDs de géneros a nombres
     */
    fun getGenreNames(genreIds: List<Int>?): List<String> {
        return genreIds?.mapNotNull { getGenreName(it) } ?: emptyList()
    }

    /**
     * Obtiene todos los géneros como lista de pares (id, nombre)
     */
    fun getAllGenres(): List<Pair<Int, String>> {
        // Combinar géneros de películas y series, sin duplicados
        val allGenres = (movieGenres + tvGenres).toSortedMap()
        return allGenres.map { (id, nombre) -> Pair(id, nombre) }
    }
}
