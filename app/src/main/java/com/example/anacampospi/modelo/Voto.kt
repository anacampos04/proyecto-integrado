package com.example.anacampospi.modelo

import com.example.anacampospi.modelo.enums.ValorVoto
import java.time.Instant

data class Voto(
    val idVoto: String = "",
    val idUsuario: String = "",
    val idContenido: String = "", // "movie:123" | "tv:456"
    val voto: ValorVoto = ValorVoto.NO_ME_GUSTA,
    val creadoEn: Instant = Instant.EPOCH
)
