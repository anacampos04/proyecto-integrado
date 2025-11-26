package com.example.anacampospi.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.LocalPizza
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Theaters
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Weekend
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Utilidad para gestionar avatares aleatorios basados en iconos de Material Design.
 * Asigna un icono consistente basado en el hash del ID del usuario.
 */
object AvatarUtil {

    /** Lista de iconos disponibles para avatares.*/
    private val AVATAR_ICONS = listOf(
        Icons.Rounded.Movie,           // Película
        Icons.Rounded.Theaters,        // Teatro/Cine
        Icons.Rounded.Videocam,        // Cámara de video
        Icons.Rounded.LiveTv,          // TV en vivo
        Icons.Rounded.Star,            // Estrella
        Icons.Rounded.Favorite,        // Corazón
        Icons.Rounded.EmojiEmotions,   // Emoji sonriente
        Icons.Rounded.SportsEsports,   // Juegos/Gaming
        Icons.Rounded.Celebration,     // Celebración
        Icons.Rounded.LocalPizza,      // Pizza
        Icons.Rounded.Coffee,          // Café
        Icons.Rounded.PhotoCamera,     // Cámara de fotos
        Icons.Rounded.Weekend,         // Sofá/relax
        Icons.Rounded.LocalBar,        // Bar/bebida
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
