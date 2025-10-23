package com.example.anacampospi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.anacampospi.repositorio.AuthRepository
import com.example.anacampospi.repositorio.UsuarioRepository
import com.example.anacampospi.ui.auth.LoginPantalla
import com.example.anacampospi.ui.auth.RegistroPantalla
import com.example.anacampospi.ui.setup.SetupInicialScreen
import com.example.anacampospi.ui.theme.PopCornTribuTheme
import com.example.anacampospi.viewModels.AuthViewModel
import com.example.anacampospi.viewModels.SetupViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PopCornTribuTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val authVm: AuthViewModel = viewModel()
    val setupVm: SetupViewModel = viewModel()

    // Determinar la ruta inicial basándose en el estado de autenticación
    val startDestination = remember {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) "home" else "login"
    }

    NavHost(navController = nav, startDestination = startDestination) {
        composable("login") {
            LoginPantalla(
                vm = authVm,
                onSuccess = {
                    // Después del login, verificar si necesita setup
                    nav.navigate("checkSetup") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoToRegister = { nav.navigate("register") }
            )
        }

        composable("register") {
            RegistroPantalla(
                vm = authVm,
                onSuccess = {
                    // Después del registro, siempre ir a setup
                    nav.navigate("setup") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onGoToLogin = { nav.popBackStack() }
            )
        }

        composable("checkSetup") {
            CheckSetupScreen(
                onSetupNeeded = {
                    nav.navigate("setup") {
                        popUpTo("checkSetup") { inclusive = true }
                    }
                },
                onSetupComplete = {
                    nav.navigate("home") {
                        popUpTo("checkSetup") { inclusive = true }
                    }
                }
            )
        }

        composable("setup") {
            SetupInicialScreen(
                vm = setupVm,
                onComplete = {
                    nav.navigate("home") {
                        popUpTo("setup") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen()
        }
    }
}

/**
 * Pantalla intermedia que verifica si el usuario necesita completar el setup inicial
 */
@Composable
fun CheckSetupScreen(
    onSetupNeeded: () -> Unit,
    onSetupComplete: () -> Unit
) {
    val authRepo = remember { AuthRepository() }
    val usuarioRepo = remember { UsuarioRepository() }

    LaunchedEffect(Unit) {
        val uid = authRepo.currentUid()
        if (uid != null) {
            try {
                val usuario = usuarioRepo.getUsuario(uid)

                // Si el usuario no tiene plataformas configuradas, necesita setup
                if (usuario?.plataformas.isNullOrEmpty()) {
                    onSetupNeeded()
                } else {
                    onSetupComplete()
                }
            } catch (e: Exception) {
                // En caso de error, ir a setup por seguridad
                onSetupNeeded()
            }
        } else {
            // No debería pasar, pero por seguridad volver a login
            onSetupNeeded()
        }
    }

    // Mostrar un loading mientras se verifica
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
fun HomeScreen() {
    val authRepo = remember { AuthRepository() }
    val usuarioRepo = remember { UsuarioRepository() }
    var codigoInvitacion by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val uid = authRepo.currentUid()
        if (uid != null) {
            val usuario = usuarioRepo.getUsuario(uid)
            codigoInvitacion = usuario?.codigoInvitacion
        }
    }

    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            "¡Has entrado! 🎬",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))

        if (codigoInvitacion != null) {
            androidx.compose.material3.Card(
                modifier = androidx.compose.ui.Modifier.padding(16.dp)
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier.padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        "Tu código de invitación:",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                    Text(
                        codigoInvitacion!!,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}