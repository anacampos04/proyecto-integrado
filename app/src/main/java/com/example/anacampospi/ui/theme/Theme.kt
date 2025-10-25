package com.example.anacampospi.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary        = TealPastel,
    onPrimary      = Color.White,
    primaryContainer = TealPastel.copy(alpha = .15f),
    onPrimaryContainer = TealDark,

    secondary      = PopcornYellow,
    onSecondary    = OutlineBrown,
    secondaryContainer = PopcornYellow.copy(alpha = .18f),
    onSecondaryContainer = OutlineBrown,

    tertiary       = CinemaRed,
    onTertiary     = Color.White,
    tertiaryContainer = CinemaRed.copy(alpha = .14f),
    onTertiaryContainer = RedDeep,

    surface        = Mist,
    onSurface      = Ink,
    surfaceVariant = Cream,
    onSurfaceVariant = OutlineBrown,

    background     = Mist,
    onBackground   = Ink,

    outline        = OutlineBrown,
    outlineVariant = Cream
)

private val DarkColors = darkColorScheme(
    primary        = TealPastel,
    onPrimary      = Color.Black,
    primaryContainer = TealDark,
    onPrimaryContainer = Color.White,

    secondary      = YellowDeep,
    onSecondary    = Color.Black,
    secondaryContainer = OutlineBrown,
    onSecondaryContainer = Color.White,

    tertiary       = RedDeep,
    onTertiary     = Color.White,
    tertiaryContainer = OutlineBrown,
    onTertiaryContainer = Color.White,

    surface        = Night,
    onSurface      = Color(0xFFEAEAEA),
    surfaceVariant = OutlineBrown,
    onSurfaceVariant = Cream,

    background     = Night,
    onBackground   = Color(0xFFEAEAEA),

    outline        = Cream,
    outlineVariant = OutlineBrown
)

/**
 * Tema principal de PopCornTribu basado en los colores del icono.
 */
@Composable
fun PopCornTribuTheme(
    useDarkTheme: Boolean = true, // SIEMPRE MODO OSCURO
    // Si quieres colores dinámicos (Android 12+), pon true:
    dynamicColor: Boolean = false, // Desactivado para forzar fondo negro
    content: @Composable () -> Unit
) {
    // Forzar tema oscuro con fondo negro puro
    val colorScheme = DarkColors.copy(
        background = Color.Black,
        surface = Color.Black,
        onBackground = Color(0xFFEAEAEA),
        onSurface = Color(0xFFEAEAEA)
    )

    // Status bar/navigation bar negras
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Black.toArgb()
            window.navigationBarColor = Color.Black.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false // Iconos blancos en fondo negro
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // definido abajo
        shapes = Shapes,
        content = content
    )
}
