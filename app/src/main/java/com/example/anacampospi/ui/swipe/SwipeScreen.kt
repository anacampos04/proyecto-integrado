package com.example.anacampospi.ui.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anacampospi.modelo.enums.TipoContenido
import com.example.anacampospi.ui.componentes.FlippableSwipeCard
import com.example.anacampospi.ui.theme.*
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
                            CircularProgressIndicator(color = TealPastel)
                            Text(
                                "Cargando contenido...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
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
                                color = CinemaRed
                            )
                            Text(
                                uiState.error ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.reiniciar() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TealPastel,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
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
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Has votado todo el contenido disponible",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Estadísticas
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = TealPastel.copy(alpha = 0.3f)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = SurfaceLight.copy(alpha = 0.8f)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        "Resumen de votación",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "❤️",
                                                style = MaterialTheme.typography.displaySmall
                                            )
                                            Text(
                                                "${uiState.totalLikes}",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = TealPastel
                                            )
                                            Text(
                                                "Me gusta",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "❌",
                                                style = MaterialTheme.typography.displaySmall
                                            )
                                            Text(
                                                "${uiState.totalDislikes}",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = CinemaRed
                                            )
                                            Text(
                                                "No me gusta",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { viewModel.reiniciar() },
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TealPastel,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reiniciar")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Volver a empezar", style = MaterialTheme.typography.titleMedium)
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

                        // Botones de acción con estilo moderno
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
                                containerColor = CinemaRed.copy(alpha = 0.2f),
                                contentColor = CinemaRed,
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
                                containerColor = TealPastel,
                                contentColor = Color.Black,
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
 * Diálogo que se muestra cuando hay un match - Estilo moderno
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
                .padding(16.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = GlowTeal,
                    spotColor = GlowTeal
                ),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceLight
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                TealPastel.copy(alpha = 0.2f),
                                SurfaceLight,
                                SurfaceLight
                            )
                        )
                    )
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Emoji de match
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.displayLarge
                )

                Text(
                    text = "¡Es un MATCH!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPastel
                )

                Text(
                    text = "Tú y tus amigos coinciden en:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceDark.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = contenido.titulo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            color = Color.White
                        )
                        if (contenido.anioEstreno > 0) {
                            Text(
                                text = contenido.anioEstreno.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
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
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPastel,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Seguir descubriendo", style = MaterialTheme.typography.titleMedium)
                }

                // Botón secundario: Ver matches
                OutlinedButton(
                    onClick = onVerMatches,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TealPastel
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                TealPastel.copy(alpha = 0.5f),
                                TealPastel.copy(alpha = 0.3f)
                            )
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Ver mis matches")
                }
            }
        }
    }
}
