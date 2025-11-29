package com.example.anacampospi.ui.tutorial

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Representa un paso del tutorial de onboarding
 */
data class TutorialStep(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector,
    val accionBoton: String = "Siguiente" // Texto del botón principal
)

/**
 * Lista de pasos del tutorial
 */
object TutorialSteps {
    val steps = listOf(
        TutorialStep(
            id = 0,
            titulo = "¡Bienvenido a PopcornTribu!",
            descripcion = "Olvídate de estar horas eligiendo qué ver. Conecta con tus amigos, haz swipe y encontrad el contenido perfecto para ver juntos.",
            icono = Icons.Rounded.MovieFilter,
            accionBoton = "Empezar"
        ),
        TutorialStep(
            id = 1,
            titulo = "Configura tu perfil",
            descripcion = "Ve a tu perfil y añade las plataformas de streaming que tengas (Netflix, HBO, Prime Video...). Esto facilitará los preparativos de las fiestas",
            icono =  Icons.Rounded.Person,
            accionBoton = "Siguiente"
        ),
        TutorialStep(
            id = 2,
            titulo = "Reúne a tu tribu",
            descripcion = "Cada usuario tiene un código único. Comparte tu código con amigos o busca el suyo en la pestaña Amigos. ¡Necesitas al menos un amigo para crear fiestas!",
            icono = Icons.Rounded.Groups,
            accionBoton = "Siguiente"
        ),
        TutorialStep(
            id = 3,
            titulo = "Los preparativos de la fiesta",
            descripcion = "Configura la fiesta a tu antojo e invita a tu tribu. ¿Noche de pelis o maratón de series? ¿Sustos o risas? Elige tus preferencias y espera a que se unan tus amigos.",
            icono = Icons.Rounded.TrackChanges,
            accionBoton = "Siguiente"
        ),
        TutorialStep(
            id = 4,
            titulo = "Swipe, Match y ¡Palomitas!",
            descripcion = "Hora de la fiesta, cuando toda la tribu esté lista empieza a hacer Swipe. Si todos coincidís, ¡tenéis un match! Lo verás en la pestaña Matches.",
            icono = Icons.Rounded.Swipe,
            accionBoton = "Siguiente"
        ),
        TutorialStep(
            id = 5,
            titulo = "¡Todo listo!",
            descripcion = "Prepara los snacks, avisa a la tribu y dale al play. ¡A disfrutar del maratón!",
            icono = Icons.Rounded.Celebration,
            accionBoton = "Empezar"
        )
    )
}
