package com.example.anacampospi.ui.componentes

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Barra de navegación inferior con botón central elevado estilo Tinder
 * 4 items: Home (izq), Swipe (centro elevado), Matches (der), Perfil (der)
 */
@Composable
fun CurvedBottomNavigationV2(
    items: List<CurvedNavItem>,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    navBarColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurface,
    centralButtonColor: Color = MaterialTheme.colorScheme.primary
) {
    // Dividir items: 2 a la izquierda, el central (Swipe), 2 a la derecha
    // Asumiendo orden: Home, Swipe, Matches, Perfil
    val leftItems = items.filter { it.route == "home" }
    val centerItem = items.first { it.route == "swipe" }
    val rightItems = items.filter { it.route == "matches" || it.route == "perfil" }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        // Barra de fondo
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter),
            color = navBarColor,
            shadowElevation = 8.dp,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Items izquierda
                leftItems.forEach { item ->
                    NavBarItemV2(
                        icon = item.icon,
                        label = item.label,
                        isSelected = currentRoute == item.route,
                        selectedColor = selectedColor,
                        unselectedColor = unselectedColor,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Espacio para el botón central
                Spacer(modifier = Modifier.weight(1f))

                // Items derecha
                rightItems.forEach { item ->
                    NavBarItemV2(
                        icon = item.icon,
                        label = item.label,
                        isSelected = currentRoute == item.route,
                        selectedColor = selectedColor,
                        unselectedColor = unselectedColor,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Botón central elevado (Swipe) - Similar a Tinder
        val isCenterSelected = currentRoute == centerItem.route
        val scale by animateFloatAsState(
            targetValue = if (isCenterSelected) 1.1f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "centerScale"
        )

        Box(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.TopCenter)
                .offset(y = 0.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onNavigate(centerItem.route) }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Círculo exterior (sombra/borde)
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(navBarColor)
            )

            // Círculo principal
            Surface(
                modifier = Modifier
                    .size(60.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                shape = CircleShape,
                color = if (isCenterSelected) centralButtonColor else centralButtonColor.copy(alpha = 0.8f),
                shadowElevation = 8.dp,
                tonalElevation = 3.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = centerItem.icon,
                        contentDescription = centerItem.label,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * Item individual de la barra de navegación (sin el central)
 */
@Composable
private fun NavBarItemV2(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) selectedColor else unselectedColor,
            modifier = Modifier
                .size(if (isSelected) 28.dp else 24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}
