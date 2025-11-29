package com.example.anacampospi.ui.componentes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.anacampospi.R
import com.example.anacampospi.ui.theme.*

/**
 * Campo de texto más moderno con efecto neumórfico sutil y animaciones.
 * Estilo con bordes redondeados, sombras difuminadas, transiciones suaves.
 */

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

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) TealPastel else TealPastel.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "borderColor"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 1.dp,
        animationSpec = tween(300),
        label = "borderWidth"
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
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                }
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = if (isFocused) GlowTeal else Color.Black.copy(alpha = 0.3f),
                    spotColor = if (isFocused) GlowTeal else Color.Black.copy(alpha = 0.3f)
                )
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
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
 * Campo de contraseña moderno con toggle para mostrar/ocultar
 */

@Composable
fun ModernPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    mostrarPassword: Boolean,
    onToggleMostrar: () -> Unit,
    isError: Boolean = false,
    supportingText: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    ModernTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        visualTransformation = if (mostrarPassword)
            androidx.compose.ui.text.input.VisualTransformation.None
        else
            PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleMostrar) {
                Icon(
                    imageVector = if (mostrarPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (mostrarPassword) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = TealPastel
                )
            }
        },
        isError = isError,
        supportingText = supportingText,
        singleLine = true,
        enabled = enabled,
        modifier = modifier
    )
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
 * Card con efecto neumórfico para mensajes de error/éxito
 */
@Composable
fun ModernMessageCard(
    message: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
    autoDismiss: Boolean = false,
    dismissDelayMillis: Long = 5000,
    onDismiss: (() -> Unit)? = null
) {
    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true

        // Auto-dismiss si está activado
        if (autoDismiss && onDismiss != null) {
            kotlinx.coroutines.delay(dismissDelayMillis)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = if (isError) CinemaRed else TealPastel,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (isError) CinemaRed else TealPastel,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
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
        verticalAlignment = Alignment.CenterVertically,
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

/**
 * Componente reutilizable para swipe-to-delete.
 * Envuelve cualquier contenido y permite eliminarlo deslizando hacia la izquierda.
 *
 * @param onDelete Callback que se ejecuta cuando se completa el deslizamiento
 * @param cornerRadius Radio de las esquinas (por defecto 12.dp)
 * @param content Contenido que se mostrará y se puede deslizar
 */

@Composable
fun SwipeToDismissItem(
    onDelete: () -> Unit,
    cornerRadius: androidx.compose.ui.unit.Dp = 12.dp,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp) // Pequeño margen para evitar el filo rojo
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(CinemaRed)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        content()
    }
}

/**
 * Botón de Google Sign-In con logo de Google
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Logo oficial de Google (usa el archivo google_logo.png que añadiste)
            Image(
                painter = painterResource(id = R.drawable.google_logo),
                contentDescription = "Google logo",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Continuar con Google",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    color = Color.White
                )
            )
        }
    }
}
