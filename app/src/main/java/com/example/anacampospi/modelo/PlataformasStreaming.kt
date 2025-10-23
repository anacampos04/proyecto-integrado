package com.example.anacampospi.modelo

/**
 * Representa las plataformas de streaming disponibles.
 * El id debe coincidir con el provider_id de TMDB para filtrar correctamente.
 */
data class PlataformaStreaming(
    val id: String,           // ID de TMDB (ej: "8" para Netflix)
    val nombre: String,       // Nombre legible
    val icono: String = ""    // URL del icono (opcional, se puede cargar de TMDB)
)

/**
 * Catálogo de plataformas soportadas en España.
 * IDs de TMDB Watch Providers para región ES.
 */
object PlataformasCatalogo {

    val NETFLIX = PlataformaStreaming(
        id = "8",
        nombre = "Netflix"
    )

    val AMAZON_PRIME = PlataformaStreaming(
        id = "119",
        nombre = "Amazon Prime Video"
    )

    val DISNEY_PLUS = PlataformaStreaming(
        id = "337",
        nombre = "Disney+"
    )

    val HBO_MAX = PlataformaStreaming(
        id = "384",
        nombre = "HBO Max"
    )

    val APPLE_TV = PlataformaStreaming(
        id = "350",
        nombre = "Apple TV+"
    )

    val MOVISTAR_PLUS = PlataformaStreaming(
        id = "149",
        nombre = "Movistar Plus+"
    )

    val FILMIN = PlataformaStreaming(
        id = "63",
        nombre = "Filmin"
    )

    val RAKUTEN = PlataformaStreaming(
        id = "35",
        nombre = "Rakuten TV"
    )

    val SKYSHOWTIME = PlataformaStreaming(
        id = "1773",
        nombre = "SkyShowtime"
    )

    /**
     * Lista completa de plataformas disponibles
     */
    val TODAS = listOf(
        NETFLIX,
        AMAZON_PRIME,
        DISNEY_PLUS,
        HBO_MAX,
        APPLE_TV,
        MOVISTAR_PLUS,
        FILMIN,
        RAKUTEN,
        SKYSHOWTIME
    )

    /**
     * Obtiene una plataforma por su ID
     */
    fun getPorId(id: String): PlataformaStreaming? {
        return TODAS.find { it.id == id }
    }

    /**
     * Convierte una lista de IDs a objetos PlataformaStreaming
     */
    fun getPlataformas(ids: List<String>): List<PlataformaStreaming> {
        return ids.mapNotNull { getPorId(it) }
    }
}