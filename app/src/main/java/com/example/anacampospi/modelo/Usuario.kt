package com.example.anacampospi.modelo

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Nota: 'amigos' aquí es un *cache* para lecturas rápidas.
 * La "fuente de verdad" será sefuramente una colección de relaciones
 */

data class Usuario(
    val idUsuario: String = "",
    val correo: String = "",
    val nombre: String = "",
    // fotoUrl eliminado - no usamos Firebase Storage
    val codigoInvitacion: String = "",
    val plataformas: List<String> = emptyList(), // plataformas que tiene el usuario
    val region: String = "ES",
    val amigos: List<String> = emptyList(), // cache de uids de amigos
    @ServerTimestamp val creadoEn:Timestamp? =null
    //@ServertTimestamp funciona cuando el valor enviado es null: Firestore pondrá la hora del servidor automáticamente en el write
)