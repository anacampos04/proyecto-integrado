package com.example.anacampospi.modelo

import com.example.anacampospi.modelo.enums.TipoContenido

data class FiltrosGrupo(
    val plataformas: List<String> = emptyList(), // unión de plataformas de todos los usuarios
    val generos: List<Int> = emptyList(), // unión de géneros de todos los usuarios (IDs de TMDb)
    val tipos: List<TipoContenido> = listOf(TipoContenido.PELICULA, TipoContenido.SERIE), // elegido por creador
    val region: String = "ES",
    val idioma: String = "es-ES"
)