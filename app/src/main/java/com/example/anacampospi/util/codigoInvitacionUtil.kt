package com.example.anacampospi.util

import kotlin.random.Random

/**
 * Utilidad para generar códigos de invitación únicos y legibles.
 * Formato: PCT-XXXX (PopCornTribu + 4 caracteres alfanuméricos)
 */
object CodigoInvitacionUtil {

    private const val PREFIX = "PCT"
    private const val CODE_LENGTH = 4
    // Caracteres que no se confunden fácilmente (sin 0, O, I, 1, etc.)
    private const val CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"

    /**
     * Genera un código de invitación con formato PCT-XXXX
     * Ejemplo: PCT-A7B9, PCT-K3M8
     */
    fun generar(): String {
        val codigo = (1..CODE_LENGTH)
            .map { CHARS[Random.nextInt(CHARS.length)] }
            .joinToString("")
        return "$PREFIX-$codigo"
    }

    /**
     * Valida el formato del código de invitación
     * @return true si el formato es correcto (PCT-XXXX)
     */
    fun esValido(codigo: String): Boolean {
        if (codigo.isBlank()) return false
        val regex = Regex("^$PREFIX-[${CHARS}]{$CODE_LENGTH}$")
        return regex.matches(codigo.uppercase())
    }

    /**
     * Normaliza el código (uppercase, sin espacios)
     */
    fun normalizar(codigo: String): String {
        return codigo.trim().uppercase()
    }
}