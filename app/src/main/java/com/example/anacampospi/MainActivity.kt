package com.example.anacampospi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.anacampospi.repositorio.AuthRepository
import com.example.anacampospi.repositorio.UsuarioRepository
import com.example.anacampospi.ui.auth.LoginPantalla
import com.example.anacampospi.ui.auth.RegistroPantalla
// VERSIÓN ACTUAL: V2 (botón central elevado estilo Tinder)
// Para volver a la versión anterior, cambia "V2" por "CurvedBottomNavigation"
import com.example.anacampospi.ui.componentes.CurvedBottomNavigationV2 as CurvedBottomNavigation
import com.example.anacampospi.ui.componentes.DefaultNavItems
import com.example.anacampospi.ui.config.ConfiguracionRondaScreen
import com.example.anacampospi.ui.matches.MatchesScreen
import com.example.anacampospi.ui.perfil.PerfilScreen
import com.example.anacampospi.ui.setup.SetupInicialScreen
import com.example.anacampospi.ui.swipe.SwipeScreen
import com.example.anacampospi.ui.theme.PopCornTribuTheme
import com.example.anacampospi.viewModels.AuthViewModel
import com.example.anacampospi.viewModels.SetupViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar splash screen ANTES de super.onCreate()
        installSplashScreen()

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

    // Estado para guardar la configuración de la ronda
    var configPlataformas by remember { mutableStateOf<List<String>?>(null) }
    var configTipo by remember { mutableStateOf<com.example.anacampospi.modelo.enums.TipoContenido?>(null) }
    var configGeneros by remember { mutableStateOf<List<Int>?>(null) }

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
                    nav.navigate("mainScreen") {
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
                    nav.navigate("mainScreen") {
                        popUpTo("setup") { inclusive = true }
                    }
                }
            )
        }

        composable("mainScreen") {
            MainScreenWithNavigation(
                configPlataformas = configPlataformas,
                configTipo = configTipo,
                configGeneros = configGeneros,
                onConfigChanged = { plataformas, tipo, generos ->
                    configPlataformas = plataformas
                    configTipo = tipo
                    configGeneros = generos
                },
                onLogout = {
                    nav.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

/**
 * Pantalla principal con navegación inferior
 */
@Composable
fun MainScreenWithNavigation(
    configPlataformas: List<String>?,
    configTipo: com.example.anacampospi.modelo.enums.TipoContenido?,
    configGeneros: List<Int>?,
    onConfigChanged: (List<String>, com.example.anacampospi.modelo.enums.TipoContenido?, List<Int>) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    Scaffold(
        bottomBar = {
            CurvedBottomNavigation(
                items = DefaultNavItems.items,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        // Pop to start destination to avoid building up back stack
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    ConfiguracionRondaScreen(
                        onIniciarRonda = { plataformas, tipo, generos ->
                            onConfigChanged(plataformas, tipo, generos)
                            navController.navigate("swipe")
                        }
                    )
                }

                composable("swipe") {
                    SwipeScreen(
                        plataformas = configPlataformas,
                        tipo = configTipo,
                        generos = configGeneros
                    )
                }

                composable("matches") {
                    MatchesScreen()
                }

                composable("perfil") {
                    PerfilScreen(
                        onLogout = onLogout
                    )
                }
            }
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

        val resultado = usuarioRepo.getUsuario(uid)
        resultado.onSuccess { usuario ->
            // Si el usuario no tiene plataformas configuradas, necesita setup
            if (usuario.plataformas.isEmpty()) {
                onSetupNeeded()
            } else {
                onSetupComplete()
            }
        }

        resultado.onFailure {
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

