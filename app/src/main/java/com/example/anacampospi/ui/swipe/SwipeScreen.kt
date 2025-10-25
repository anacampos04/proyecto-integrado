package com.example.anacampospi.ui.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anacampospi.modelo.enums.TipoContenido
import com.example.anacampospi.ui.componentes.FlippableSwipeCard
import com.example.anacampospi.viewModels.SwipeViewModel

/**
 * Pantalla principal de swipe para votar contenido
 */
@Composable
fun SwipeScreen(
    plataformas: List<String>? = null,
    tipo: TipoContenido? = null,
    generos: List<Int>? = null,
    viewModel: SwipeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar contenido con filtros al iniciar
    LaunchedEffect(plataformas, tipo, generos) {
        viewModel.cargarContenido(
            tipo = tipo,
            plataformas = plataformas,
            generos = generos
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
            when {
                // Estado de carga
                uiState.loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Cargando contenido...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Error
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "Error al cargar contenido",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                uiState.error ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = { viewModel.reiniciar() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reintentar")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reintentar")
                            }
                        }
                    }
                }

                // Sin contenido
                uiState.sinContenido -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "🎬",
                                style = MaterialTheme.typography.displayLarge
                            )
                            Text(
                                "¡Has terminado!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Has votado todo el contenido disponible",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Estadísticas
                            Card(
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        "Resumen de votación",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "❤️",
                                                style = MaterialTheme.typography.headlineMedium
                                            )
                                            Text(
                                                "${uiState.totalLikes}",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Me gusta",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "❌",
                                                style = MaterialTheme.typography.headlineMedium
                                            )
                                            Text(
                                                "${uiState.totalDislikes}",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "No me gusta",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { viewModel.reiniciar() },
                                modifier = Modifier.fillMaxWidth(0.6f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reiniciar")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Volver a empezar")
                            }
                        }
                    }
                }

                // Mostrar tarjeta
                uiState.contenidoActual != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Tarjeta swipeable con flip
                        // Usar key para forzar recomposición cuando cambia el contenido
                        key(uiState.contenidoActual!!.idContenido) {
                            FlippableSwipeCard(
                                contenido = uiState.contenidoActual!!,
                                onSwipeLeft = { viewModel.onSwipeLeft() },
                                onSwipeRight = { viewModel.onSwipeRight() },
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(top = 8.dp, bottom = 16.dp)
                            )
                        }

                        // Botones de acción
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 32.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón NO ME GUSTA
                            FloatingActionButton(
                                onClick = { viewModel.onSwipeLeft() },
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "No me gusta",
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // Botón ME GUSTA
                            FloatingActionButton(
                                onClick = { viewModel.onSwipeRight() },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Me gusta",
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            }

        // Diálogo de match
        if (uiState.hayMatch && uiState.contenidoMatch != null) {
            MatchDialog(
                contenido = uiState.contenidoMatch!!,
                onContinuar = {
                    viewModel.continuarDespuesDeMatch()
                },
                onVerMatches = {
                    // Por ahora solo continuar, la pantalla de matches se implementará después
                    viewModel.continuarDespuesDeMatch()
                }
            )
        }
    }
}

/**
 * Diálogo que se muestra cuando hay un match
 */
@Composable
fun MatchDialog(
    contenido: com.example.anacampospi.modelo.ContenidoLite,
    onContinuar: () -> Unit,
    onVerMatches: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = { /* No permitir cerrar sin acción */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Emoji o icono de match
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.displayLarge
                )

                Text(
                    text = "¡Es un MATCH!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Tú y tus amigos coinciden en:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = contenido.titulo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 2
                        )
                        if (contenido.anioEstreno > 0) {
                            Text(
                                text = contenido.anioEstreno.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón principal: Continuar votando
                Button(
                    onClick = onContinuar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Seguir descubriendo", style = MaterialTheme.typography.titleMedium)
                }

                // Botón secundario: Ver matches (pendiente)
                OutlinedButton(
                    onClick = onVerMatches,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver mis matches")
                }
            }
        }
    }
}
