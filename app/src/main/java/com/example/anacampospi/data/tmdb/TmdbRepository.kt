package com.example.anacampospi.data.tmdb

import com.example.anacampospi.data.tmdb.models.TmdbMovie
import com.example.anacampospi.data.tmdb.models.TmdbTvShow
import com.example.anacampospi.data.tmdb.models.getProviderIds
import com.example.anacampospi.data.tmdb.models.toContenidoLite
import com.example.anacampospi.modelo.ContenidoLite
import com.example.anacampospi.modelo.enums.TipoContenido
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar las llamadas a la API de TMDb
 */
class TmdbRepository(private val apiService: TmdbApiService) {

    companion object {
        private const val REGION_ES = "ES"
        private const val LANGUAGE_ES = "es-ES"
    }

    /**
     * Obtiene contenido popular (películas y series mezcladas)
     */
    suspend fun getPopularContent(
        page: Int = 1,
        includeMovies: Boolean = true,
        includeTv: Boolean = true
    ): Result<List<ContenidoLite>> = withContext(Dispatchers.IO) {
        try {
            val content = mutableListOf<ContenidoLite>()

            if (includeMovies) {
                val moviesResponse = apiService.getPopularMovies(page)
                val movies = moviesResponse.results.map { movie ->
                    async {
                        val providers = fetchMovieProviders(movie.id)
                        val trailer = fetchMovieTrailer(movie.id)
                        movie.toContenidoLite(providers, trailer)
                    }
                }.awaitAll()
                content.addAll(movies)
            }

            if (includeTv) {
                val tvResponse = apiService.getPopularTvShows(page)
                val tvShows = tvResponse.results.map { tvShow ->
                    async {
                        val providers = fetchTvShowProviders(tvShow.id)
                        val trailer = fetchTvShowTrailer(tvShow.id)
                        tvShow.toContenidoLite(providers, trailer)
                    }
                }.awaitAll()
                content.addAll(tvShows)
            }

            // Mezclar y ordenar por popularidad (asumiendo que voteAverage es indicador)
            Result.success(content.shuffled())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Descubre contenido con filtros específicos
     */
    suspend fun discoverContent(
        tipo: TipoContenido? = null,
        plataformas: List<String>? = null,
        generos: List<Int>? = null,
        minRating: Double? = null,
        page: Int = 1
    ): Result<List<ContenidoLite>> = withContext(Dispatchers.IO) {
        try {
            val content = mutableListOf<ContenidoLite>()

            val platformsParam = plataformas?.joinToString("|")
            // Usar "|" para unión (OR) de géneros, no "," que es intersección (AND)
            val genresParam = generos?.joinToString("|")

            when (tipo) {
                TipoContenido.PELICULA -> {
                    val moviesResponse = apiService.discoverMovies(
                        page = page,
                        withWatchProviders = platformsParam,
                        withGenres = genresParam,
                        minRating = minRating
                    )
                    val movies = moviesResponse.results.map { movie ->
                        async {
                            val providers = fetchMovieProviders(movie.id)
                            val trailer = fetchMovieTrailer(movie.id)
                            movie.toContenidoLite(providers, trailer)
                        }
                    }.awaitAll()
                    content.addAll(movies)
                }
                TipoContenido.SERIE -> {
                    val tvResponse = apiService.discoverTvShows(
                        page = page,
                        withWatchProviders = platformsParam,
                        withGenres = genresParam,
                        minRating = minRating
                    )
                    val tvShows = tvResponse.results.map { tvShow ->
                        async {
                            val providers = fetchTvShowProviders(tvShow.id)
                            val trailer = fetchTvShowTrailer(tvShow.id)
                            tvShow.toContenidoLite(providers, trailer)
                        }
                    }.awaitAll()
                    content.addAll(tvShows)
                }
                null -> {
                    // Obtener ambos tipos
                    val moviesResponse = apiService.discoverMovies(
                        page = page,
                        withWatchProviders = platformsParam,
                        withGenres = genresParam,
                        minRating = minRating
                    )
                    val tvResponse = apiService.discoverTvShows(
                        page = page,
                        withWatchProviders = platformsParam,
                        withGenres = genresParam,
                        minRating = minRating
                    )

                    val movies = moviesResponse.results.map { movie ->
                        async {
                            val providers = fetchMovieProviders(movie.id)
                            val trailer = fetchMovieTrailer(movie.id)
                            movie.toContenidoLite(providers, trailer)
                        }
                    }.awaitAll()
                    val tvShows = tvResponse.results.map { tvShow ->
                        async {
                            val providers = fetchTvShowProviders(tvShow.id)
                            val trailer = fetchTvShowTrailer(tvShow.id)
                            tvShow.toContenidoLite(providers, trailer)
                        }
                    }.awaitAll()

                    content.addAll(movies)
                    content.addAll(tvShows)
                }
            }

            Result.success(content.shuffled())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca contenido por texto
     */
    suspend fun searchContent(
        query: String,
        tipo: TipoContenido? = null,
        page: Int = 1
    ): Result<List<ContenidoLite>> = withContext(Dispatchers.IO) {
        try {
            val content = mutableListOf<ContenidoLite>()

            when (tipo) {
                TipoContenido.PELICULA -> {
                    val moviesResponse = apiService.searchMovies(query, page)
                    val movies = moviesResponse.results.map { movie ->
                        async {
                            val providers = fetchMovieProviders(movie.id)
                            val trailer = fetchMovieTrailer(movie.id)
                            movie.toContenidoLite(providers, trailer)
                        }
                    }.awaitAll()
                    content.addAll(movies)
                }
                TipoContenido.SERIE -> {
                    val tvResponse = apiService.searchTvShows(query, page)
                    val tvShows = tvResponse.results.map { tvShow ->
                        async {
                            val providers = fetchTvShowProviders(tvShow.id)
                            val trailer = fetchTvShowTrailer(tvShow.id)
                            tvShow.toContenidoLite(providers, trailer)
                        }
                    }.awaitAll()
                    content.addAll(tvShows)
                }
                null -> {
                    val moviesResponse = apiService.searchMovies(query, page)
                    val tvResponse = apiService.searchTvShows(query, page)

                    val movies = moviesResponse.results.map { movie ->
                        async {
                            val providers = fetchMovieProviders(movie.id)
                            val trailer = fetchMovieTrailer(movie.id)
                            movie.toContenidoLite(providers, trailer)
                        }
                    }.awaitAll()
                    val tvShows = tvResponse.results.map { tvShow ->
                        async {
                            val providers = fetchTvShowProviders(tvShow.id)
                            val trailer = fetchTvShowTrailer(tvShow.id)
                            tvShow.toContenidoLite(providers, trailer)
                        }
                    }.awaitAll()

                    content.addAll(movies)
                    content.addAll(tvShows)
                }
            }

            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los proveedores de streaming para una película
     */
    private suspend fun fetchMovieProviders(movieId: Int): List<String> {
        return try {
            val response = apiService.getMovieWatchProviders(movieId)
            response.results[REGION_ES]?.getProviderIds() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene los proveedores de streaming para una serie
     */
    private suspend fun fetchTvShowProviders(tvShowId: Int): List<String> {
        return try {
            val response = apiService.getTvShowWatchProviders(tvShowId)
            response.results[REGION_ES]?.getProviderIds() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene el trailer principal de una película (primer trailer oficial de YouTube)
     */
    private suspend fun fetchMovieTrailer(movieId: Int): String? {
        return try {
            val response = apiService.getMovieVideos(movieId)
            // Buscar trailer oficial en YouTube
            response.results
                .filter { it.site == "YouTube" && it.type == "Trailer" && it.official == true }
                .sortedByDescending { it.size }
                .firstOrNull()?.key
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene el trailer principal de una serie
     */
    private suspend fun fetchTvShowTrailer(tvShowId: Int): String? {
        return try {
            val response = apiService.getTvShowVideos(tvShowId)
            // Buscar trailer oficial en YouTube
            response.results
                .filter { it.site == "YouTube" && it.type == "Trailer" && it.official == true }
                .sortedByDescending { it.size }
                .firstOrNull()?.key
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene detalles de una película por ID
     */
    suspend fun getMovieDetails(movieId: Int): Result<TmdbMovie> = withContext(Dispatchers.IO) {
        try {
            val movie = apiService.getMovieDetails(movieId)
            Result.success(movie)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene detalles de una serie por ID
     */
    suspend fun getTvShowDetails(tvShowId: Int): Result<TmdbTvShow> = withContext(Dispatchers.IO) {
        try {
            val tvShow = apiService.getTvShowDetails(tvShowId)
            Result.success(tvShow)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
