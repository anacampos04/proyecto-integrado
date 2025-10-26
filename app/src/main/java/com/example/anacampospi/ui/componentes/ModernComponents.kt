package com.example.anacampospi.ui.componentes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.anacampospi.ui.theme.*

/**
 * Campo de texto más moderno con efecto neumórfico sutil y animaciones.
 * Estilo con bordes redondeados, sombras difuminadas, transiciones suaves.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    // Animación sutil al enfocarse
    var isFocused by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 4.dp else 2.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "elevation"
    )

    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = null) }
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            enabled = enabled,
            isError = isError,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight.copy(alpha = 0.8f),
                disabledContainerColor = SurfaceDark,
                errorContainerColor = SurfaceLight,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedLabelColor = TealPastel,
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = if (isFocused) GlowTeal else Color.Black.copy(alpha = 0.3f),
                    spotColor = if (isFocused) GlowTeal else Color.Black.copy(alpha = 0.3f)
                )
        )

        // Texto de soporte con animación
        AnimatedVisibility(
            visible = supportingText != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isError) CinemaRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}

/**
 * Botón moderno con gradiente sutil y efecto de elevación.
 * Incluye animaciones microinteractivas.
 */
@Composable
fun ModernButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    isPrimary: Boolean = true
) {
    // Animación de escala al presionar
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .height(56.dp)
            .shadow(
                elevation = if (enabled) 8.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isPrimary) GlowTeal else Color.Black.copy(alpha = 0.2f),
                spotColor = if (isPrimary) GlowTeal else Color.Black.copy(alpha = 0.2f)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) TealPastel else SurfaceLight,
            contentColor = if (isPrimary) Color.Black else Color.White,
            disabledContainerColor = SurfaceDark,
            disabledContentColor = Color.White.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = if (isPrimary) Color.Black else TealPastel,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * Botón outlined moderno (para Google, etc)
 */
@Composable
fun ModernOutlinedButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(56.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.4f)
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    TealPastel.copy(alpha = 0.5f),
                    TealPastel.copy(alpha = 0.3f)
                )
            ),
            width = 1.5.dp
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (icon != null) {
                Spacer(modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * Card con efecto neumórfico para mensajes de error/éxito
 */
@Composable
fun ModernMessageCard(
    message: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = if (isError) CinemaRed.copy(alpha = 0.3f) else TealPastel.copy(alpha = 0.3f),
                    spotColor = if (isError) CinemaRed.copy(alpha = 0.3f) else TealPastel.copy(alpha = 0.3f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isError)
                    CinemaRed.copy(alpha = 0.15f)
                else
                    TealPastel.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                OverlayWhite05,
                                Color.Transparent
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Divider moderno con gradiente
 */
@Composable
fun ModernDivider(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.1f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.1f)
        )
    }
}

/**
 * TextButton moderno con efecto hover sutil
 */
@Composable
fun ModernTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = TealPastel
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
