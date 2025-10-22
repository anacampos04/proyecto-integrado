package com.example.anacampospi.ui.componentes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PcTOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            // Texto
            focusedTextColor = cs.onSurface,
            unfocusedTextColor = cs.onSurface,
            disabledTextColor = cs.onSurface.copy(alpha = 0.38f),

            // Fondo del input
            focusedContainerColor = cs.surface,
            unfocusedContainerColor = cs.surface,
            disabledContainerColor = cs.surfaceVariant,

            // Borde (para OutlinedTextField)
            focusedIndicatorColor = cs.primary,
            unfocusedIndicatorColor = cs.outline,
            disabledIndicatorColor = cs.outline.copy(alpha = 0.4f),

            // Label / placeholder / cursor
            focusedLabelColor = cs.primary,
            unfocusedLabelColor = cs.onSurfaceVariant,
            disabledLabelColor = cs.onSurfaceVariant.copy(alpha = 0.38f),
            focusedPlaceholderColor = cs.onSurfaceVariant.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = cs.onSurfaceVariant.copy(alpha = 0.6f),
            cursorColor = cs.primary
        )
    )
}
