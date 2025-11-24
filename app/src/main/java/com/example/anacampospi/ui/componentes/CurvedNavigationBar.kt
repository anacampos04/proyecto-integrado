package com.example.anacampospi.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anacampospi.R
import com.example.anacampospi.ui.theme.CinemaRed
import com.example.anacampospi.ui.theme.Night
import com.example.anacampospi.ui.theme.TealPastel

/**
 * Representa un icono que puede ser ImageVector o Drawable
 */
sealed class NavIcon {
    data class Vector(val imageVector: ImageVector) : NavIcon()
    data class Drawable(@DrawableRes val resId: Int) : NavIcon()
}

/**
 * Ítem de navegación para la barra curva
 */
data class CurvedNavItem(
    val route: String,
    val icon: NavIcon,
    val label: String,
    val badgeCount: Int = 0 // Contador para el badge
) {
    // Constructor de conveniencia para ImageVector
    constructor(route: String, imageVector: ImageVector, label: String, badgeCount: Int = 0) : this(
        route,
        NavIcon.Vector(imageVector),
        label,
        badgeCount
    )

    // Constructor de conveniencia para Drawable
    constructor(route: String, @DrawableRes drawableRes: Int, label: String, badgeCount: Int = 0) : this(
        route,
        NavIcon.Drawable(drawableRes),
        label,
        badgeCount
    )
}

/**
 * Barra de navegación con curva hacia arriba (mejorado)
 * Adaptada para usar CurvedNavItem (soporta ImageVector y Drawable)
 * Con animaciones fluidas de transición de iconos
 */
@Composable
fun CurvedNavigationBar(
    items: List<CurvedNavItem>,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    barHeight: Dp = 64.dp,
    circleRadius: Dp = 28.dp,
    curveRadius: Dp = 38.dp,
) {
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    // Configuración de la animación (FastOutSlowInEasing para suavidad)
    val animationSpec = tween<Float>(durationMillis = 400, easing = FastOutSlowInEasing)

    // Animación de la posición X de la curva
    val selectedItemFloat by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = animationSpec,
        label = "curve_position"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight + circleRadius)
            .background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. El Fondo Curvo (Canvas)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter)
                .shadow(8.dp)
        ) {
            val width = size.width
            val height = size.height
            val itemWidth = width / items.size

            val curveCenterX = (itemWidth * selectedItemFloat) + (itemWidth / 2)
            val curveRadiusPx = curveRadius.toPx()

            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(curveCenterX - curveRadiusPx * 1.5f, 0f)
                cubicTo(
                    curveCenterX - curveRadiusPx, 0f,
                    curveCenterX - curveRadiusPx, height * 0.6f,
                    curveCenterX, height * 0.6f
                )
                cubicTo(
                    curveCenterX + curveRadiusPx, height * 0.6f,
                    curveCenterX + curveRadiusPx, 0f,
                    curveCenterX + curveRadiusPx * 1.5f, 0f
                )
                lineTo(width, 0f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(path = path, color = Night, style = Fill)
        }

        // 2. El Botón Flotante con TRANSICIÓN DE ICONO
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val itemWidth = maxWidth / items.size
            val offsetX = (itemWidth * selectedItemFloat) + (itemWidth / 2) - circleRadius

            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = 0.dp)
                    .size(circleRadius * 2)
                    .shadow(10.dp, shape = CircleShape)
                    .background(TealPastel, CircleShape)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                // TRANSICIÓN ANIMADA DE ICONOS (MUY SUAVE Y SUTIL)
                AnimatedContent(
                    targetState = items[selectedIndex],
                    transitionSpec = {
                        // Transición ultra suave con fade y escala muy sutil
                        (fadeIn(
                            animationSpec = tween(
                                durationMillis = 500,
                                delayMillis = 50,
                                easing = FastOutSlowInEasing
                            )
                        ) + scaleIn(
                            initialScale = 0.92f, // Escala más cercana a 1.0 para cambio sutil
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing
                            )
                        ))
                            .togetherWith(
                                // El viejo sale difuminándose y reduciéndose levemente
                                fadeOut(
                                    animationSpec = tween(
                                        durationMillis = 450,
                                        easing = FastOutSlowInEasing
                                    )
                                ) + scaleOut(
                                    targetScale = 0.92f, // Escala similar para simetría
                                    animationSpec = tween(
                                        durationMillis = 450,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            )
                    },
                    label = "icon_transition"
                ) { targetItem ->
                    // Renderizar icono según tipo (Vector o Drawable)
                    when (val icon = targetItem.icon) {
                        is NavIcon.Vector -> Icon(
                            imageVector = icon.imageVector,
                            contentDescription = targetItem.label,
                            tint = Color.Black,
                            modifier = Modifier.fillMaxSize()
                        )
                        is NavIcon.Drawable -> Icon(
                            painter = painterResource(id = icon.resId),
                            contentDescription = targetItem.label,
                            tint = Color.Black,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // 3. Iconos de la barra con ANIMACIÓN DE SALIDA/ENTRADA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                // Anima la escala y opacidad del icono inferior de forma muy suave
                // Si está seleccionado -> Escala 0 (desaparece)
                // Si NO está seleccionado -> Escala 1 (aparece)
                val iconScale by animateFloatAsState(
                    targetValue = if (index == selectedIndex) 0f else 1f,
                    animationSpec = tween(
                        durationMillis = 450,
                        easing = FastOutSlowInEasing
                    ),
                    label = "icon_scale_$index"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(item.route) },
                    contentAlignment = Alignment.Center
                ) {
                    // Box contenedor del icono + badge
                    Box {
                        // Renderizar icono con animación de escala y alpha
                        when (val icon = item.icon) {
                            is NavIcon.Vector -> Icon(
                                imageVector = icon.imageVector,
                                contentDescription = item.label,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(26.dp)
                                    .scale(iconScale)
                                    .alpha(iconScale)
                            )
                            is NavIcon.Drawable -> Icon(
                                painter = painterResource(id = icon.resId),
                                contentDescription = item.label,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(30.dp)
                                    .scale(iconScale)
                                    .alpha(iconScale)
                            )
                        }

                        // Badge para notificaciones (solo si no está seleccionado y tiene count > 0)
                        if (item.badgeCount > 0 && index != selectedIndex) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(16.dp)
                                    .background(CinemaRed, CircleShape)
                                    .scale(iconScale)
                                    .alpha(iconScale),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Text(
                                    text = if (item.badgeCount > 9) "9+" else item.badgeCount.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper para crear los items de navegación por defecto
 */
object DefaultNavItems {
    val items = listOf(
        CurvedNavItem("home", R.drawable.ic_home, "Inicio"),
        CurvedNavItem("amigos", Icons.Rounded.People, "Amigos"),
        CurvedNavItem("swipe", R.drawable.ic_popcorn, "Swipes"),
        CurvedNavItem("matches", Icons.Rounded.Favorite, "Matches"),
        CurvedNavItem("perfil", Icons.Rounded.Person, "Perfil")
    )
}
