package com.example.anacampospi.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.anacampospi.R
import com.example.anacampospi.auth.GoogleSignInHelper
import com.example.anacampospi.viewModels.AuthViewModel
import com.example.anacampospi.ui.componentes.*
import com.example.anacampospi.ui.theme.*

@Composable
fun LoginPantalla(
    vm: AuthViewModel,
    onSuccess: () -> Unit,
    onGoToRegister: () -> Unit
) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    val activity = ctx as Activity
    val webClientId = ctx.getString(R.string.default_web_client_id)

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }

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
        if (pass.isNotEmpty() && pass.length < 6) {
            "Mínimo 6 caracteres"
        } else null
    }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        runCatching {
            val account = GoogleSignInHelper.getAccountFromIntent(res.data)
            vm.loginWithGoogle(account.idToken!!)
        }
    }

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
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo con animación de entrada
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600)) +
                        slideInVertically(
                            initialOffsetY = { -100 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icono de palomitas con gradiente (El Box crea el fondo)
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = GlowTeal,
                                spotColor = GlowTeal
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        TealPastel,
                                        TealDark // El gradiente azul/verde que quieres
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            // Usa el nombre de tu archivo PNG de palomitas SIN fondo
                            painter = painterResource(id = R.drawable.popcorn_tribu_icon_clean),
                            contentDescription = "Icono de PopCorn Tribu",
                            modifier = Modifier
                                .size(100.dp) // Tamaño del dibujo dentro del Box (120dp)
                        )
                    }

                    Text(
                        text = "PopCornTribu",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Descubre películas y series\ncon tus amigos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                    // Campo de email
                    ModernTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo electrónico",
                        leadingIcon = Icons.Default.Email,
                        isError = emailError != null,
                        supportingText = emailError
                    )

                    // Campo de contraseña
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

                    // Botón de recuperar contraseña
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ModernTextButton(
                            onClick = { showResetPasswordDialog = true },
                            text = "¿Olvidaste tu contraseña?"
                        )
                    }
                }
            }

            // Mensajes de estado
            AnimatedVisibility(visible = state.resetEmailSent) {
                ModernMessageCard(
                    message = "✅ Te hemos enviado un correo para recuperar tu contraseña. Revisa tu bandeja de entrada.",
                    isError = false
                )
            }

            state.error?.let { error ->
                ModernMessageCard(
                    message = error,
                    isError = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botones con animación
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Botón de login
                    ModernButton(
                        onClick = { vm.login(email.trim(), pass) },
                        text = "Iniciar sesión",
                        enabled = !state.loading &&
                                email.isNotEmpty() &&
                                pass.isNotEmpty() &&
                                emailError == null &&
                                passError == null,
                        loading = state.loading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Divider
                    ModernDivider(text = "o")

                    // Botón de Google
                    ModernOutlinedButton(
                        onClick = {
                            val intent = GoogleSignInHelper.intent(activity, webClientId)
                            googleLauncher.launch(intent)
                        },
                        text = "Continuar con Google",
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Link a registro
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 600))
            ) {
                ModernTextButton(
                    onClick = onGoToRegister,
                    text = "¿No tienes cuenta? Regístrate",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Diálogo para recuperar contraseña
    if (showResetPasswordDialog) {
        ResetPasswordDialog(
            onDismiss = { showResetPasswordDialog = false },
            onConfirm = { resetEmail ->
                vm.resetPassword(resetEmail)
                showResetPasswordDialog = false
            }
        )
    }
}

@Composable
fun ResetPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var resetEmail by remember { mutableStateOf("") }

    val emailError = remember(resetEmail) {
        if (resetEmail.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmail)
                .matches()
        ) {
            "Email inválido"
        } else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Recuperar contraseña",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Introduce tu email y te enviaremos un enlace para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                ModernTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    label = "Correo electrónico",
                    leadingIcon = Icons.Default.Email,
                    isError = emailError != null,
                    supportingText = emailError
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(resetEmail.trim()) },
                enabled = resetEmail.isNotEmpty() && emailError == null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPastel,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}