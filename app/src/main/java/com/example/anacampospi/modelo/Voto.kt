package com.example.anacampospi.modelo

import com.example.anacampospi.modelo.enums.ValorVoto
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Voto(
    val idVoto: String = "",
    val idUsuario: String = "",
    val idContenido: String = "", // "movie:123" | "tv:456"
    val voto: ValorVoto = ValorVoto.NO_ME_GUSTA,
    @ServerTimestamp val creadoEn: Timestamp? = null
)
