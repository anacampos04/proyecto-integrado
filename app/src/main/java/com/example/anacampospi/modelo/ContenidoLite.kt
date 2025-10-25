package com.example.anacampospi.modelo
import com.example.anacampospi.modelo.enums.TipoContenido

data class ContenidoLite(
    val idContenido: String = "",
    val titulo: String = "",
    val posterUrl: String = "",
    val tipo: TipoContenido = TipoContenido.PELICULA,
    val anioEstreno: Int = 0,
    val proveedores: List<String> = emptyList(),
    val puntuacion: Double = 0.0,
    val sinopsis: String? = null,
    val generos: List<String> = emptyList(),
    val trailer: Trailer? = null
)
