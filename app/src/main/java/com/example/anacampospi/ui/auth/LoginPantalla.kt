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
import androidx.compose.ui.layout.ContentScale
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
            // Header con imagen y gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
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
                                endY = 1200f
                            )
                        )
                )

                // Título sobre el gradiente
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "PopCornTribu",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "Descubre películas y series con tus amigos",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        message = "Te hemos enviado un correo para recuperar tu contraseña. Revisa tu bandeja de entrada.",
                        isError = false
                    )
                }

                state.error?.let { error ->
                    ModernMessageCard(
                        message = error,
                        isError = true
                    )
                }

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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
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

                        // Botón de Google con logo
                        GoogleSignInButton(
                            onClick = {
                                val intent = GoogleSignInHelper.intent(activity, webClientId)
                                googleLauncher.launch(intent)
                            },
                            enabled = !state.loading
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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

                // Spacer para llenar el espacio restante y evitar franjas blancas
                Spacer(modifier = Modifier.height(24.dp))
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