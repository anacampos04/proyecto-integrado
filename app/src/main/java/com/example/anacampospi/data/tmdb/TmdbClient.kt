package com.example.anacampospi.data.tmdb

import com.example.anacampospi.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente singleton para configurar Retrofit con TMDb API
 */
object TmdbClient {

    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val TIMEOUT_SECONDS = 30L

    /**
     * Interceptor que añade la API key a todas las peticiones
     */
    private class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url

            // Añadir api_key como query parameter
            val urlWithApiKey = originalUrl.newBuilder()
                .addQueryParameter("api_key", apiKey)
                .build()

            val requestWithApiKey = originalRequest.newBuilder()
                .url(urlWithApiKey)
                .build()

            return chain.proceed(requestWithApiKey)
        }
    }

    /**
     * Crea el cliente OkHttp con interceptores
     */
    private fun createOkHttpClient(apiKey: String): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(apiKey))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Crea la instancia de Retrofit
     */
    private fun createRetrofit(apiKey: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(apiKey))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Crea una instancia del servicio de API
     */
    fun createService(apiKey: String): TmdbApiService {
        return createRetrofit(apiKey).create(TmdbApiService::class.java)
    }

    /**
     * Crea una instancia del repositorio con el servicio configurado
     */
    fun createRepository(apiKey: String): TmdbRepository {
        return TmdbRepository(createService(apiKey))
    }
}
