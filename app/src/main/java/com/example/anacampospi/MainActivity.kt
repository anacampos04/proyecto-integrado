package com.example.anacampospi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
import com.example.anacampospi.ui.componentes.CurvedNavigationBar
import com.example.anacampospi.ui.componentes.CurvedNavItem
import com.example.anacampospi.ui.componentes.DefaultNavItems
import com.example.anacampospi.ui.amigos.AmigosScreen
import com.example.anacampospi.ui.config.ConfiguracionRondaScreen
import com.example.anacampospi.ui.home.HomeScreen
import com.example.anacampospi.ui.matches.MatchesScreen
import com.example.anacampospi.ui.perfil.PerfilScreen
import com.example.anacampospi.ui.swipe.SwipeScreen
import com.example.anacampospi.ui.theme.PopCornTribuTheme
import com.example.anacampospi.viewModels.AmigosViewModel
import com.example.anacampospi.viewModels.AuthViewModel
import com.example.anacampospi.ui.tutorial.TutorialViewModel
import com.example.anacampospi.ui.tutorial.TutorialOverlay
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    // MutableState para notificar cambios en el Intent
    private val _intentState = mutableStateOf<Intent?>(null)
    val intentState: State<Intent?> = _intentState

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar splash screen ANTES de super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Procesar intent inicial
        _intentState.value = intent
        // Firebase pasa los datos del payload "data" directamente como extras
        val tipoNotificacion = intent.getStringExtra("tipo")
        android.util.Log.d("MainActivity", "onCreate - Tipo de notificación: $tipoNotificacion")

        setContent {
            PopCornTribuTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Actualizar el estado para que Compose lo detecte
        _intentState.value = intent
        val tipoNotificacion = intent.getStringExtra("tipo")
        android.util.Log.d("MainActivity", "onNewIntent - Tipo de notificación: $tipoNotificacion")
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

    // ViewModel para obtener el contador de solicitudes pendientes
    val amigosViewModel: AmigosViewModel = viewModel()
    val amigosState by amigosViewModel.uiState.collectAsState()

    // ViewModel del tutorial
    val tutorialViewModel: TutorialViewModel = viewModel()
    val tutorialState by tutorialViewModel.uiState.collectAsState()

    // Solicitar permiso de notificaciones en Android 13+ (API 33+)
    val context = LocalContext.current

    // Observar cambios en el Intent desde MainActivity
    val activity = context as? MainActivity
    val currentIntent by (activity?.intentState ?: remember { mutableStateOf<Intent?>(null) })

    // Extraer el tipo de notificación del Intent actual
    // Firebase pasa los datos del campo "data" directamente como extras
    val notificacionTipo = currentIntent?.getStringExtra("tipo")

    // Log para debugging
    LaunchedEffect(currentIntent) {
        android.util.Log.d("MainActivity", "Intent cambió - tipo: $notificacionTipo")
    }

    // Launcher para solicitar el permiso
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "Permiso de notificaciones concedido")
        } else {
            android.util.Log.w("MainActivity", "Permiso de notificaciones denegado")
        }
    }

    // Verificar si debe mostrarse el tutorial
    LaunchedEffect(Unit) {
        if (tutorialViewModel.deberMostrarTutorial(context)) {
            android.util.Log.d("MainActivity", "Primera vez - mostrando tutorial")
            tutorialViewModel.mostrarTutorial()
        }
    }

    // Solicitar permiso al entrar a la pantalla principal (solo una vez)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                android.util.Log.d("MainActivity", "Solicitando permiso de notificaciones")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                android.util.Log.d("MainActivity", "Permiso de notificaciones ya concedido")
            }
        }
    }

    // Navegar basado en el tipo de notificación
    LaunchedEffect(notificacionTipo) {
        if (notificacionTipo != null) {
            android.util.Log.d("MainActivity", "Detectada notificación tipo: $notificacionTipo")

            // Pequeño delay para asegurar que el NavHost esté listo
            kotlinx.coroutines.delay(100)

            android.util.Log.d("MainActivity", "Navegando a pantalla correspondiente...")
            when (notificacionTipo) {
                "peticion_amistad" -> {
                    android.util.Log.d("MainActivity", "Navegando a amigos")
                    navController.navigate("amigos") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
                "invitacion_ronda" -> {
                    android.util.Log.d("MainActivity", "Navegando a home (invitación)")
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                    }
                }
                "ronda_activada" -> {
                    android.util.Log.d("MainActivity", "Navegando a home (activada)")
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                    }
                }
                "nuevo_match" -> {
                    android.util.Log.d("MainActivity", "Navegando a matches")
                    navController.navigate("matches") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }

            android.util.Log.d("MainActivity", "Navegación completada, limpiando extra")
            // Limpiar el extra para evitar navegaciones repetidas
            currentIntent?.removeExtra("tipo")
        }
    }

    // Mapear rutas internas a rutas de la navbar
    val mappedRoute = when {
        // Configuración y swipe -> marcar "swipe" en navbar
        actualRoute == "configurarRonda" -> "swipe"
        actualRoute.startsWith("configurarRonda/") -> "swipe" // Usuario invitado configurando
        actualRoute.startsWith("swipe/") -> "swipe"

        // Pantallas de matches -> marcar "matches" en navbar
        actualRoute.startsWith("matchesFrom/") -> "matches" // Matches con grupo pre-seleccionado
        actualRoute.startsWith("matches/") -> "matches" // Matches de grupo específico

        else -> actualRoute
    }

    // Asegurar que siempre hay un icono marcado (si la ruta no está en navbar, usar "home")
    val navItemRoutes = DefaultNavItems.items.map { it.route }
    val currentRoute = if (mappedRoute in navItemRoutes) mappedRoute else "home"

    // Crear lista de items con badge actualizado para "amigos"
    val navItemsWithBadge = DefaultNavItems.items.map { item ->
        if (item.route == "amigos") {
            // Actualizar el badgeCount para el botón de amigos
            CurvedNavItem(
                route = item.route,
                icon = item.icon,
                label = item.label,
                badgeCount = amigosState.solicitudesPendientes.size
            )
        } else {
            item
        }
    }

    Scaffold(
        bottomBar = {
            CurvedNavigationBar(
                items = navItemsWithBadge,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    // Navegación desde la navbar: siempre debe funcionar correctamente
                    // y volver a la pantalla principal correspondiente
                    when (route) {
                        "home" -> {
                            // Home: limpiar todo el stack y volver a home
                            navController.navigate("home") {
                                popUpTo("home") {
                                    inclusive = true
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                        "swipe" -> {
                            // Swipe: ir a configurar ronda (nueva ronda)
                            navController.navigate("configurarRonda") {
                                popUpTo("home") {
                                    inclusive = false
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                        else -> {
                            // Otras rutas (amigos, matches, perfil)
                            navController.navigate(route) {
                                popUpTo("home") {
                                    inclusive = false
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
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
                        },
                        onVerMatches = {
                            // Navegar a matches general con el grupo pre-seleccionado
                            if (grupoId != null) {
                                navController.navigate("matchesFrom/$grupoId")
                            } else {
                                navController.navigate("matches")
                            }
                        }
                    )
                }

                composable("amigos") {
                    AmigosScreen(viewModel = amigosViewModel)
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
                    // Modo general: todos los grupos
                    MatchesScreen()
                }

                composable("matchesFrom/{grupoId}") { backStackEntry ->
                    // Modo general pero con filtro pre-seleccionado del grupo
                    val grupoIdInicial = backStackEntry.arguments?.getString("grupoId")
                    MatchesScreen(
                        grupoIdInicial = grupoIdInicial
                    )
                }

                composable("matches/{grupoId}") { backStackEntry ->
                    // Modo específico: matches de un grupo concreto
                    val grupoId = backStackEntry.arguments?.getString("grupoId")
                    val grupoNombre = backStackEntry.arguments?.getString("grupoNombre")
                    MatchesScreen(
                        grupoId = grupoId,
                        grupoNombre = grupoNombre
                    )
                }

                composable("perfil") {
                    PerfilScreen(
                        onLogout = onLogout
                    )
                }
            }

            // Overlay del tutorial que se muestra sobre todo el contenido
            TutorialOverlay(
                viewModel = tutorialViewModel,
                uiState = tutorialState
            )
        }
    }
}

