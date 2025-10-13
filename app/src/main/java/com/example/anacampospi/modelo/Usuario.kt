package com.example.anacampospi.modelo

import java.time.Instant

data class Usuario(
    val idUsuario: String = "",
    val correo: String = "",
    val nombre: String = "",
    val fotoUrl: String = "",
    val codigoInvitacion: String = "",
    val plataformas: List<String> = emptyList(), // plataformas que tiene el usuario
    val region: String = "ES",
    val amigos: List<String> = emptyList(), // ids de otros usuarios
    val creadoEn: Instant = Instant.EPOCH
)