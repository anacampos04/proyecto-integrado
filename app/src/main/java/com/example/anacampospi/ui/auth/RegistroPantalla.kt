@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.anacampospi.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.anacampospi.viewModels.AuthViewModel
import com.example.anacampospi.ui.componentes.*
import com.example.anacampospi.ui.theme.*

@Composable
fun RegistroPantalla(
    vm: AuthViewModel,
    onSuccess: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val state by vm.state.collectAsState()

    var nombreUsuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Animaciones de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        vm.resetState()
    }

    // Validaciones
    val emailError = remember(email) {
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Email inválido"
        } else null
    }

    val passError = remember(pass) {
        when {
            pass.isEmpty() -> null
            pass.length < 6 -> "Mínimo 6 caracteres"
            !pass.any { it.isDigit() } -> "Debe contener al menos un número"
            else -> null
        }
    }

    val confirmPassError = remember(pass, confirmPass) {
        if (confirmPass.isNotEmpty() && pass != confirmPass) {
            "Las contraseñas no coinciden"
        } else null
    }

    val formValido = email.isNotEmpty() &&
            nombreUsuario.isNotEmpty() &&
            pass.isNotEmpty() &&
            confirmPass.isNotEmpty() &&
            emailError == null &&
            passError == null &&
            confirmPassError == null

    LaunchedEffect(state.success) {
        if (state.success) onSuccess()
    }

    // Fondo con gradiente sutil
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black,
                        Night,
                        Color.Black
                    ),
                    startY = 0f,
                    endY = 1500f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { -it })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onGoToLogin) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Crear cuenta",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header con animación
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 100)) +
                            slideInVertically(
                                initialOffsetY = { -50 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "¡Únete a PopCornTribu! 🎬",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Crea tu cuenta para empezar a descubrir\ncontenido con tus amigos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Campos de entrada con animación escalonada
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                            slideInVertically(
                                initialOffsetY = { 100 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Nombre de usuario
                        ModernTextField(
                            value = nombreUsuario,
                            onValueChange = { nombreUsuario = it },
                            label = "Nombre de usuario",
                            leadingIcon = Icons.Default.Person
                        )

                        // Email
                        ModernTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Correo electrónico",
                            leadingIcon = Icons.Default.Email,
                            isError = emailError != null,
                            supportingText = emailError
                        )

                        // Contraseña
                        ModernTextField(
                            value = pass,
                            onValueChange = { pass = it },
                            label = "Contraseña",
                            leadingIcon = Icons.Default.Lock,
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                        tint = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            isError = passError != null,
                            supportingText = passError
                        )

                        // Confirmar contraseña
                        ModernTextField(
                            value = confirmPass,
                            onValueChange = { confirmPass = it },
                            label = "Confirmar contraseña",
                            leadingIcon = Icons.Default.Lock,
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        if (confirmPasswordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Ocultar" else "Mostrar",
                                        tint = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            isError = confirmPassError != null,
                            supportingText = confirmPassError
                        )
                    }
                }

                // Card de requisitos con animación
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 300)) +
                            expandVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = TealPastel.copy(alpha = 0.2f)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceLight.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Requisitos de contraseña:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            ModernRequisitoItem("Mínimo 6 caracteres", pass.length >= 6)
                            ModernRequisitoItem("Al menos un número", pass.any { it.isDigit() })
                        }
                    }
                }

                // Mensaje de error
                state.error?.let { error ->
                    ModernMessageCard(
                        message = error,
                        isError = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de registro con animación
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) +
                            slideInVertically(
                                initialOffsetY = { 100 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                ) {
                    ModernButton(
                        onClick = {
                            vm.register(
                                email = email.trim(),
                                password = pass,
                                nombre = nombreUsuario.trim()
                            )
                        },
                        text = "Crear cuenta",
                        enabled = formValido && !state.loading,
                        loading = state.loading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Link a login
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 500))
                ) {
                    ModernTextButton(
                        onClick = onGoToLogin,
                        text = "¿Ya tienes cuenta? Inicia sesión",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/*@Composable
fun RequisitoItem(texto: String, cumplido: Boolean) {
    ModernRequisitoItem(texto, cumplido)
}*/

@Composable
fun ModernRequisitoItem(texto: String, cumplido: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icono con animación
        Icon(
            imageVector = if (cumplido) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            tint = if (cumplido) TealPastel else Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp)
        )

        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = if (cumplido)
                Color.White.copy(alpha = 0.9f)
            else
                Color.White.copy(alpha = 0.5f),
            fontWeight = if (cumplido) FontWeight.Medium else FontWeight.Normal
        )
    }
}