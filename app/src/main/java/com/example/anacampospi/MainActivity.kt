package com.example.anacampospi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
// VERSIÓN ACTUAL: V1 (navbar con curva elegante)
import com.example.anacampospi.ui.componentes.CurvedBottomNavigation
import com.example.anacampospi.ui.componentes.DefaultNavItems
import com.example.anacampospi.ui.amigos.AmigosScreen
import com.example.anacampospi.ui.config.ConfiguracionRondaScreen
import com.example.anacampospi.ui.home.HomeScreen
import com.example.anacampospi.ui.matches.MatchesScreen
import com.example.anacampospi.ui.perfil.PerfilScreen
import com.example.anacampospi.ui.swipe.SwipeScreen
import com.example.anacampospi.ui.theme.PopCornTribuTheme
import com.example.anacampospi.viewModels.AuthViewModel
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

    // Determinar inicio basado en si hay sesión activa
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) "mainScreen" else "login"

    NavHost(navController = nav, startDestination = startDestination) {
        composable("login") {
            // Verificar que NO haya sesión activa
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                // Si hay sesión, ir a mainScreen
                LaunchedEffect(Unit) {
                    nav.navigate("mainScreen") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                LoginPantalla(
                    vm = authVm,
                    onSuccess = {
                        // Después del login, ir directamente a mainScreen
                        nav.navigate("mainScreen") {
                            popUpTo(0) { inclusive = true } // Limpiar todo el stack
                        }
                    },
                    onGoToRegister = { nav.navigate("register") }
                )
            }
        }

        composable("register") {
            RegistroPantalla(
                vm = authVm,
                onSuccess = {
                    // Después del registro, ir directamente a mainScreen
                    nav.navigate("mainScreen") {
                        popUpTo(0) { inclusive = true } // Limpiar todo el stack
                    }
                },
                onGoToLogin = { nav.popBackStack() }
            )
        }

        composable("mainScreen") {
            // Verificar que SÍ haya sesión activa
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                // Si NO hay sesión, ir a login
                LaunchedEffect(Unit) {
                    nav.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                MainScreenWithNavigation(
                    onLogout = {
                        nav.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Pantalla principal con navegación inferior
 */
@Composable
fun MainScreenWithNavigation(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val actualRoute = navBackStackEntry?.destination?.route ?: "home"

    // Mapear rutas internas a rutas de la navbar
    // Cuando estamos en configurarRonda o swipe/{grupoId}, marcar "swipe" en la navbar
    val currentRoute = when {
        actualRoute == "configurarRonda" -> "swipe"
        actualRoute.startsWith("swipe/") -> "swipe"
        else -> actualRoute
    }

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
                    HomeScreen(
                        onNuevaRonda = {
                            navController.navigate("configurarRonda")
                        },
                        onGrupoClick = { grupoId ->
                            navController.navigate("swipe/$grupoId")
                        },
                        onConfigurarRonda = { grupoId ->
                            navController.navigate("configurarRonda/$grupoId")
                        }
                    )
                }

                composable("configurarRonda") {
                    ConfiguracionRondaScreen(
                        grupoId = null, // Modo creador
                        onIniciarRonda = { grupoId, irASwipes ->
                            if (irASwipes) {
                                // Ir directamente a swipes
                                navController.navigate("swipe/$grupoId") {
                                    popUpTo("home") { inclusive = false }
                                }
                            } else {
                                // Volver a Home para ver el estado de la ronda
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable("configurarRonda/{grupoId}") { backStackEntry ->
                    val grupoId = backStackEntry.arguments?.getString("grupoId")
                    ConfiguracionRondaScreen(
                        grupoId = grupoId, // Modo invitado
                        onIniciarRonda = { id, irASwipes ->
                            if (irASwipes) {
                                // Ir directamente a swipes (último en configurar)
                                navController.navigate("swipe/$id") {
                                    popUpTo("home") { inclusive = false }
                                }
                            } else {
                                // Volver a Home para ver el estado de la ronda
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable("swipe/{grupoId}") { backStackEntry ->
                    val grupoId = backStackEntry.arguments?.getString("grupoId")
                    SwipeScreen(
                        grupoId = grupoId,
                        onBack = {
                            // Volver a home cuando el usuario sale de swipes
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }

                composable("amigos") {
                    AmigosScreen()
                }

                composable("swipe") {
                    // Ruta temporal para compatibilidad - redirige a config
                    LaunchedEffect(Unit) {
                        navController.navigate("configurarRonda") {
                            popUpTo("swipe") { inclusive = true }
                        }
                    }
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

