package com.example.anacampospi.modelo

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Grupo(
    val idGrupo: String = "",
    val nombre: String = "",
    val miembros: List<String> = emptyList(), // ids de usuarios
    val creadoPor: String = "", // id del usuario que ha creado el grupo
    val filtros: FiltrosGrupo = FiltrosGrupo(),
    @ServerTimestamp val filtrosActualizadosEn: Timestamp? = null,
    @ServerTimestamp val creadoEn: Timestamp? =null
)
