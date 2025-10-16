package com.example.anacampospi.modelo

import com.example.anacampospi.modelo.enums.TipoContenido
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


data class Match(
    val idContenido: String = "",
    val usuariosCoincidentes: List<String> = emptyList(), // unanimidad = tamaño == miembros del grupo
    @ServerTimestamp val primerCoincidenteEn: Timestamp? = null,
    val actualizadoEn: Timestamp? = null,
    // metadatos de catálogo para pintar tarjetas/listado
    val titulo: String = "",
    val posterUrl: String = "",
    val tipo: TipoContenido = TipoContenido.PELICULA,
    val anioEstreno: Int = 0,
    val proveedores: List<String> = emptyList(),
    val puntuacion: Double = 0.0
)