package com.example.anacampospi.data.tmdb

import com.example.anacampospi.data.tmdb.models.TmdbMovie
import com.example.anacampospi.data.tmdb.models.TmdbMovieResponse
import com.example.anacampospi.data.tmdb.models.TmdbTvResponse
import com.example.anacampospi.data.tmdb.models.TmdbTvShow
import com.example.anacampospi.data.tmdb.models.TmdbVideosResponse
import com.example.anacampospi.data.tmdb.models.TmdbWatchProvidersResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de Retrofit para las llamadas a la API de TMDb
 */
interface TmdbApiService {

    /**
     * Obtiene películas populares
     * @param page Número de página (default: 1)
     * @param language Idioma de los resultados (default: es-ES)
     * @param region Región para filtrado (default: ES)
     */
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "es-ES",
        @Query("region") region: String = "ES"
    ): TmdbMovieResponse

    /**
     * Obtiene series populares
     */
    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "es-ES"
    ): TmdbTvResponse

    /**
     * Descubre películas con filtros
     * @param withWatchProviders IDs de plataformas separados por | (ej: "8|119" para Netflix y Prime)
     * @param watchRegion Región para proveedores (default: ES)
     */
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "es-ES",
        @Query("region") region: String = "ES",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_watch_providers") withWatchProviders: String? = null,
        @Query("watch_region") watchRegion: String = "ES",
        @Query("with_genres") withGenres: String? = null,
        @Query("vote_average.gte") minRating: Double? = null,
        @Query("vote_count.gte") minVoteCount: Int? = 50
    ): TmdbMovieResponse

    /**
     * Descubre series con filtros
     */
    @GET("discover/tv")
    suspend fun discoverTvShows(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "es-ES",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_watch_providers") withWatchProviders: String? = null,
        @Query("watch_region") watchRegion: String = "ES",
        @Query("with_genres") withGenres: String? = null,
        @Query("vote_average.gte") minRating: Double? = null,
        @Query("vote_count.gte") minVoteCount: Int? = 50
    ): TmdbTvResponse

    /**
     * Busca películas por texto
     */
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "es-ES",
        @Query("region") region: String = "ES",
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbMovieResponse

    /**
     * Busca series por texto
     */
    @GET("search/tv")
    suspend fun searchTvShows(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "es-ES",
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbTvResponse

    /**
     * Obtiene detalles de una película
     */
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "es-ES"
    ): TmdbMovie

    /**
     * Obtiene detalles de una serie
     */
    @GET("tv/{tv_id}")
    suspend fun getTvShowDetails(
        @Path("tv_id") tvId: Int,
        @Query("language") language: String = "es-ES"
    ): TmdbTvShow

    /**
     * Obtiene los proveedores de streaming (watch providers) para una película
     */
    @GET("movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProviders(
        @Path("movie_id") movieId: Int
    ): TmdbWatchProvidersResponse

    /**
     * Obtiene los proveedores de streaming para una serie
     */
    @GET("tv/{tv_id}/watch/providers")
    suspend fun getTvShowWatchProviders(
        @Path("tv_id") tvId: Int
    ): TmdbWatchProvidersResponse

    /**
     * Obtiene los videos/trailers de una película
     */
    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "es-ES"
    ): TmdbVideosResponse

    /**
     * Obtiene los videos/trailers de una serie
     */
    @GET("tv/{tv_id}/videos")
    suspend fun getTvShowVideos(
        @Path("tv_id") tvId: Int,
        @Query("language") language: String = "es-ES"
    ): TmdbVideosResponse
}
