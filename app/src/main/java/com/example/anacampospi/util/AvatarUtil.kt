package com.example.anacampospi.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Utilidad para gestionar avatares aleatorios basados en iconos de Material Design.
 * Asigna un icono consistente basado en el hash del ID del usuario.
 */
object AvatarUtil {

    /** Lista de iconos disponibles para avatares.*/
    private val AVATAR_ICONS = listOf(
        Icons.Filled.Movie,           // Película
        Icons.Filled.Theaters,        // Teatro/Cine
        Icons.Filled.Videocam,        // Cámara de video
        Icons.Filled.LiveTv,          // TV en vivo
        Icons.Filled.Star,            // Estrella
        Icons.Filled.Favorite,        // Corazón
        Icons.Filled.EmojiEmotions,   // Emoji sonriente
        Icons.Filled.SportsEsports,   // Juegos/Gaming
        Icons.Filled.Celebration,     // Celebración
        Icons.Filled.LocalPizza,      // Pizza
        Icons.Filled.Coffee,          // Café
        Icons.Filled.PhotoCamera,     // Cámara de fotos
        Icons.Filled.Weekend,         // Sofá/relax
        Icons.Filled.LocalBar,        // Bar/bebida
    )

    /**
     * Obtiene un icono de avatar basado en el ID del usuario.
     * Usa el hash del ID para asignar siempre el mismo icono al mismo usuario.
     *
     * @param userId ID del usuario
     * @return ImageVector del icono asignado
     */
    fun getAvatarIcon(userId: String): ImageVector {
        val hash = userId.hashCode()
        val index = Math.abs(hash % AVATAR_ICONS.size)
        return AVATAR_ICONS[index]
    }

    /**
     * Obtiene todos los iconos disponibles para mostrar en un selector.
     * @return Lista completa de iconos de avatar
     */
    fun getAllIcons(): List<ImageVector> = AVATAR_ICONS
}
