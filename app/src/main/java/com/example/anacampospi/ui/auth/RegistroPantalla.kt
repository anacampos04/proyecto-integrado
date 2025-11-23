@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.anacampospi.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.anacampospi.R
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
        if (pass.isEmpty()) {
            null
        } else {
            val errors = mutableListOf<String>()
            if (pass.length < 6) errors.add("mínimo 6 caracteres")
            if (!pass.any { it.isDigit() }) errors.add("al menos un número")

            if (errors.isNotEmpty()) {
                "Falta: ${errors.joinToString(", ")}"
            } else {
                null
            }
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

    // Navegar cuando el registro sea exitoso
    LaunchedEffect(state.success) {
        if (state.success) {
            onSuccess()
        }
    }

    // Fondo con imagen y gradiente
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Fondo negro para evitar franjas blancas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con imagen y gradiente + TopBar superpuesto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                // Imagen de fondo
                Image(
                    painter = painterResource(id = R.drawable.login_background),
                    contentDescription = "Movie posters background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradiente de imagen a fondo negro
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.8f),
                                    Color.Black
                                ),
                                startY = 0f,
                                endY = 900f
                            )
                        )
                )

                // Top Bar superpuesto con fondo más transparente
                if (visible) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
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

                // Título sobre el gradiente
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (visible) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "¡Únete a PopCornTribu!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Crea tu cuenta para empezar a descubrir contenido con tus amigos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            }

            // Contenido del formulario
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

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