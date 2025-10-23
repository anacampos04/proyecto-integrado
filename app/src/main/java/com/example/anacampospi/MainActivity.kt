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

    // Determinar inicio basado en si hay sesión activa
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) "checkSetup" else "login"

    NavHost(navController = nav, startDestination = startDestination) {
        composable("login") {
            // No auto-navegar desde login, solo cuando el usuario hace login exitoso
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
                },
                onNotAuthenticated = {
                    // Si llegó aquí sin autenticación, volver a login
                    nav.navigate("login") {
                        popUpTo(0) { inclusive = true }
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
            HomeScreen(
                onLogout = {
                    // Cerrar sesión de Firebase
                    FirebaseAuth.getInstance().signOut()
                    // Navegar a login y limpiar todo el back stack
                    nav.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

/**
 * Pantalla intermedia que verifica si el usuario necesita completar el setup inicial
 */
@Composable
fun CheckSetupScreen(
    onSetupNeeded: () -> Unit,
    onSetupComplete: () -> Unit,
    onNotAuthenticated: () -> Unit = {}
) {
    val authRepo = remember { AuthRepository() }
    val usuarioRepo = remember { UsuarioRepository() }

    LaunchedEffect(Unit) {
        // Verificar directamente si hay usuario autenticado en Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            // Si no hay usuario autenticado, esto no debería pasar
            // pero si pasa, volver a setup (o login si se implementa)
            onNotAuthenticated()
            return@LaunchedEffect
        }

        val uid = currentUser.uid
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
fun HomeScreen(onLogout: () -> Unit) {
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

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(32.dp))

        // Botón temporal de cerrar sesión
        androidx.compose.material3.OutlinedButton(
            onClick = onLogout,
            modifier = androidx.compose.ui.Modifier
                .padding(horizontal = 32.dp)
        ) {
            Text("Cerrar sesión")
        }
    }
}