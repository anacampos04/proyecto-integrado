package com.example.anacampospi.modelo

import com.example.anacampospi.modelo.enums.TipoContenido

data class FiltrosGrupo(
    val plataformas: List<String> = emptyList(), // unión de plataformas
    val tipos: List<TipoContenido> = listOf(TipoContenido.PELICULA, TipoContenido.SERIE),
    val region: String = "ES",
    val idioma: String = "es-ES"
)