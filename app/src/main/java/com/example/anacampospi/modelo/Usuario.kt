package com.example.anacampospi.modelo

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Usuario(
    val idUsuario: String = "",
    val correo: String = "",
    val nombre: String = "",
    val fotoUrl: String = "",
    val codigoInvitacion: String = "",
    val plataformas: List<String> = emptyList(), // plataformas que tiene el usuario
    val region: String = "ES",
    val amigos: List<String> = emptyList(), // ids de otros usuarios
    @ServerTimestamp val creadoEn:Timestamp? =null
    //@ServertTimestamp funciona cuando el valor enviado es null: Firestore prondrá la hora del servidor automáticamente en el write
)