package com.example.anacampospi.modelo

import java.time.Instant

data class Grupo(
    val idGrupo: String = "",
    val nombre: String = "",
    val miembros: List<String> = emptyList(), // ids de usuarios
    val creadoPor: String = "", // id del usuario que ha creado el grupo
    val filtros: FiltrosGrupo = FiltrosGrupo(),
    val filtrosActualizadosEn: Instant = Instant.EPOCH,
    val creadoEn: Instant = Instant.EPOCH
)
