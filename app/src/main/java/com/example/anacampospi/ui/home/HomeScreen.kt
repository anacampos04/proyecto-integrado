package com.example.anacampospi.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anacampospi.modelo.Grupo
import com.example.anacampospi.ui.componentes.SwipeToDismissItem
import com.example.anacampospi.ui.theme.*
import com.example.anacampospi.viewModels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla principal (Home) que muestra las rondas activas del usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNuevaRonda: () -> Unit,
    onGrupoClick: (String) -> Unit, // ID del grupo para navegar a swipe
    onConfigurarRonda: (String) -> Unit = {}, // ID del grupo para navegar a configurar
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        viewModel.cargarGrupos() // Refrescar al entrar a la pantalla
    }

    // Mostrar mensaje de error
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.limpiarError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black,
                        Night,
                        Color.Black
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top bar con saludo
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { -it })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Column {
                        Text(
                            text = "Hola${if (uiState.nombreUsuario.isNotBlank()) ", ${uiState.nombreUsuario}" else ""}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "¿Qué vamos a ver hoy?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón para crear nueva ronda
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(500, 100)) + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500, 100)
                )
            ) {
                NuevaRondaCard(onClick = onNuevaRonda)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Contenido principal
            if (uiState.cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PopcornYellow)
                }
            } else if (uiState.grupos.isEmpty()) {
                // Estado vacío
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(500, 200))
                ) {
                    EstadoVacio()
                }
            } else {
                // Mostrar las tres secciones
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // 1. Rondas activas (listas para swipe)
                    if (uiState.rondasActivas.isNotEmpty()) {
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(animationSpec = tween(500, 200)) + slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(500, 200)
                            )
                        ) {
                            SeccionRondasActivas(
                                grupos = uiState.rondasActivas,
                                onGrupoClick = onGrupoClick,
                                onEliminar = { viewModel.eliminarGrupo(it) }
                            )
                        }
                    }

                    // 2. Rondas pendientes (necesitas configurar)
                    if (uiState.rondasPendientes.isNotEmpty()) {
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(animationSpec = tween(500, 250)) + slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(500, 250)
                            )
                        ) {
                            SeccionRondasPendientes(
                                grupos = uiState.rondasPendientes,
                                onConfigurarClick = onConfigurarRonda,
                                onEliminar = { viewModel.eliminarGrupo(it) }
                            )
                        }
                    }

                    // 3. Rondas esperando (ya configuraste, falta que otros configuren)
                    if (uiState.rondasEsperando.isNotEmpty()) {
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(animationSpec = tween(500, 300)) + slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(500, 300)
                            )
                        ) {
                            SeccionRondasEsperando(
                                grupos = uiState.rondasEsperando,
                                onGrupoClick = onGrupoClick, // Por si quieren ver detalles
                                onEliminar = { viewModel.eliminarGrupo(it) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Mensaje de error
        uiState.error?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Card grande para crear nueva ronda
 */
@Composable
fun NuevaRondaCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = GlowTeal,
                spotColor = GlowTeal
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            TealPastel,
                            TealDark
                        )
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icono
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Texto
                Column {
                    Text(
                        text = "Empezar nueva fiesta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Invita amigos y vota juntos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Sección 1: Rondas activas (listas para hacer swipe)
 */
@Composable
fun SeccionRondasActivas(
    grupos: List<Grupo>,
    onGrupoClick: (String) -> Unit,
    onEliminar: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "🟢",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Listas para votar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            grupos.forEach { grupo ->
                GrupoItem(
                    grupo = grupo,
                    onClick = { onGrupoClick(grupo.idGrupo) },
                    onEliminar = { onEliminar(grupo.idGrupo) },
                    badge = "ACTIVA"
                )
            }
        }
    }
}

/**
 * Sección 2: Rondas pendientes (necesitas configurar tus preferencias)
 */
@Composable
fun SeccionRondasPendientes(
    grupos: List<Grupo>,
    onConfigurarClick: (String) -> Unit,
    onEliminar: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "⏳",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Pendientes de configurar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PopcornYellow
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            grupos.forEach { grupo ->
                GrupoItem(
                    grupo = grupo,
                    onClick = { onConfigurarClick(grupo.idGrupo) },
                    onEliminar = { onEliminar(grupo.idGrupo) },
                    badge = "CONFIGURAR"
                )
            }
        }
    }
}

/**
 * Sección 3: Rondas esperando (ya configuraste, esperando a otros)
 */
@Composable
fun SeccionRondasEsperando(
    grupos: List<Grupo>,
    onGrupoClick: (String) -> Unit,
    onEliminar: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "⏸️",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Esperando a otros",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            grupos.forEach { grupo ->
                GrupoItem(
                    grupo = grupo,
                    onClick = { onGrupoClick(grupo.idGrupo) },
                    onEliminar = { onEliminar(grupo.idGrupo) },
                    badge = "ESPERANDO",
                    mostrarUsuariosPendientes = true
                )
            }
        }
    }
}

/**
 * Item individual de grupo/ronda
 */
@Composable
fun GrupoItem(
    grupo: Grupo,
    onClick: () -> Unit,
    onEliminar: () -> Unit,
    badge: String? = null,
    mostrarUsuariosPendientes: Boolean = false
) {
    val badgeColor = when (badge) {
        "ACTIVA" -> TealPastel
        "CONFIGURAR" -> PopcornYellow
        "ESPERANDO" -> Color.White.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    SwipeToDismissItem(
        onDelete = onEliminar,
        cornerRadius = 16.dp
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Icono
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(PopcornYellow, CinemaRed)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Groups,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = grupo.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        // Badge
                        if (badge != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = badgeColor
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${grupo.miembros.size} ${if (grupo.miembros.size == 1) "persona" else "personas"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    // Mostrar usuarios pendientes si es necesario
                    if (mostrarUsuariosPendientes) {
                        val pendientes = grupo.usuariosPendientes()
                        if (pendientes.isNotEmpty()) {
                            Text(
                                text = "Esperando a ${pendientes.size} ${if (pendientes.size == 1) "persona" else "personas"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PopcornYellow.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Fecha de creación si existe
                    grupo.creadoEn?.let { timestamp ->
                        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(timestamp.seconds * 1000))
                        Text(
                            text = "Creado: $fecha",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
        }
    }
}

/**
 * Estado vacío cuando no hay rondas
 */
@Composable
fun EstadoVacio() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Movie,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No hay rondas activas",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Crea una nueva fiesta para empezar a votar con tus amigos",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}
