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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
 * Barra de navegación inferior con curva animada
 * Inspirada en rn-curved-navigation-bar
 */
@Composable
fun CurvedBottomNavigation(
    items: List<CurvedNavItem>,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    navBarColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedBackgroundColor: Color = Color.Black,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val density = LocalDensity.current

    // Animación suave del índice seleccionado
    val animatedSelectedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "selectedIndex"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        // Fondo con la curva
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    with(density) {
                        shadowElevation = 8.dp.toPx()
                    }
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    clip = true
                }
        ) {
            val curveWidth = with(density) { 100.dp.toPx() }
            val curveHeight = with(density) { 40.dp.toPx() }
            val itemWidth = size.width / items.size
            val curveCenter = itemWidth * (animatedSelectedIndex + 0.5f)

            val path = Path().apply {
                moveTo(0f, 0f)

                // Línea hasta el inicio de la curva
                lineTo(curveCenter - curveWidth / 2, 0f)

                // Curva hacia arriba (bezier cuadrática)
                quadraticBezierTo(
                    curveCenter - curveWidth / 4, 0f,
                    curveCenter - curveWidth / 4, -curveHeight / 2
                )

                // Curva del arco superior
                quadraticBezierTo(
                    curveCenter, -curveHeight,
                    curveCenter + curveWidth / 4, -curveHeight / 2
                )

                // Curva bajando
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

            drawPath(
                path = path,
                color = navBarColor
            )
        }

        // Iconos de navegación
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = currentRoute == item.route
                val offsetY by animateDpAsState(
                    targetValue = if (isSelected) (-28).dp else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "offsetY"
                )

                NavBarItem(
                    icon = item.icon,
                    label = item.label,
                    isSelected = isSelected,
                    selectedBackgroundColor = selectedBackgroundColor,
                    selectedColor = selectedColor,
                    unselectedColor = unselectedColor,
                    offsetY = offsetY,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Item individual de la barra de navegación
 */
@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    selectedBackgroundColor: Color,
    selectedColor: Color,
    unselectedColor: Color,
    offsetY: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
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
        // Solo iconos, sin labels
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Círculo de fondo para el ítem seleccionado
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(selectedBackgroundColor)
                        .graphicsLayer {
                            shadowElevation = 8f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = selectedColor,
                        modifier = Modifier
                            .size(28.dp)
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
                    tint = unselectedColor,
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
