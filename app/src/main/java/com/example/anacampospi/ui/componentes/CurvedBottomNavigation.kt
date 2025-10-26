package com.example.anacampospi.ui.componentes

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.anacampospi.ui.theme.*
import kotlin.math.abs

/**
 * Ítem de navegación para la barra curva
 */
data class CurvedNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

/**
 * Barra de navegación inferior con curva animada invertida - Estilo moderno 2025
 * La curva ahora simula esquinas redondeadas de la pantalla sobre la navbar
 */
@Composable
fun CurvedBottomNavigation(
    items: List<CurvedNavItem>,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val density = LocalDensity.current

    // Animación más fluida del índice seleccionado
    val animatedSelectedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "selectedIndex"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        // Fondo con la curva invertida (esquinas redondeadas de la pantalla)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    with(density) {
                        shadowElevation = 16.dp.toPx()
                    }
                    ambientShadowColor = GlowTeal
                    spotShadowColor = GlowTeal
                }
        ) {
            val curveWidth = with(density) { 80.dp.toPx() }
            val curveDepth = with(density) { 20.dp.toPx() }
            val itemWidth = size.width / items.size
            val curveCenter = itemWidth * (animatedSelectedIndex + 0.5f)

            val path = Path().apply {
                moveTo(0f, 0f)

                // Línea hasta el inicio de la curva invertida
                lineTo(curveCenter - curveWidth / 2, 0f)

                // Curva hacia abajo (invertida) - lado izquierdo
                quadraticBezierTo(
                    curveCenter - curveWidth / 4, 0f,
                    curveCenter - curveWidth / 4, curveDepth / 2
                )

                // Curva del arco inferior (invertida)
                quadraticBezierTo(
                    curveCenter, curveDepth,
                    curveCenter + curveWidth / 4, curveDepth / 2
                )

                // Curva subiendo - lado derecho
                quadraticBezierTo(
                    curveCenter + curveWidth / 4, 0f,
                    curveCenter + curveWidth / 2, 0f
                )

                // Línea hasta el final
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            // Fondo con gradiente moderno
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfaceLight.copy(alpha = 0.95f),
                        SurfaceDark.copy(alpha = 0.95f)
                    )
                )
            )
        }

        // Iconos de navegación
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = currentRoute == item.route
                val offsetY by animateDpAsState(
                    targetValue = if (isSelected) (-8).dp else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "offsetY"
                )

                NavBarItem(
                    icon = item.icon,
                    label = item.label,
                    isSelected = isSelected,
                    offsetY = offsetY,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Item individual de la barra de navegación - Estilo moderno
 */
@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    offsetY: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animación más fluida de escala
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )

    // Animación de alpha para transición más suave
    val alpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.6f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Círculo de fondo con gradiente para el ítem seleccionado
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            ambientColor = GlowTeal,
                            spotColor = GlowTeal
                        )
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    TealPastel,
                                    TealDark
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.Black,
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White.copy(alpha = alpha),
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }
        }
    }
}

/**
 * Helper para crear los items de navegación por defecto
 */
object DefaultNavItems {
    val items = listOf(
        CurvedNavItem("home", Icons.Default.Home, "Inicio"),
        CurvedNavItem("swipe", Icons.Default.Favorite, "Swipes"),
        CurvedNavItem("matches", Icons.Default.Star, "Matches"),
        CurvedNavItem("perfil", Icons.Default.Person, "Perfil")
    )
}
