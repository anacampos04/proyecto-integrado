package com.example.anacampospi.modelo

import com.example.anacampospi.modelo.enums.TipoContenido
import java.time.Instant

data class Match(
    val idContenido: String = "",
    val usuariosCoincidentes: List<String> = emptyList(), // unanimidad = tamaño == miembros del grupo
    val primerCoincidenteEn: Instant = Instant.EPOCH,
    val actualizadoEn: Instant = Instant.EPOCH,
    // metadatos de catálogo para pintar tarjetas/listado
    val titulo: String = "",
    val posterUrl: String = "",
    val tipo: TipoContenido = TipoContenido.PELICULA,
    val anioEstreno: Int = 0,
    val proveedores: List<String> = emptyList(),
    val puntuacion: Double = 0.0
)