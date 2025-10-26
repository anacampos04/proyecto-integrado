@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.anacampospi.ui.setup

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.anacampospi.modelo.PlataformaStreaming
import com.example.anacampospi.modelo.PlataformasCatalogo
import com.example.anacampospi.viewModels.SetupViewModel
import com.example.anacampospi.ui.componentes.*
import com.example.anacampospi.ui.theme.*

/**
 * Pantalla de configuración inicial que se muestra la primera vez
 * que el usuario entra a la app después de registrarse.
 */
@Composable
fun SetupInicialScreen(
    vm: SetupViewModel,
    onComplete: () -> Unit
) {
    val state by vm.state.collectAsState()

    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    LaunchedEffect(state.completado) {
        if (state.completado) onComplete()
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
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { -it })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Configura tu perfil",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Contenido principal
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Título y descripción con animación
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 100)) +
                            slideInVertically(initialOffsetY = { -50 })
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Logo animado
                        Box(
                            modifier = Modifier
                                .size(80.dp)
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
                                            TealDark
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎬",
                                style = MaterialTheme.typography.displayMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "¡Bienvenido a PopCornTribu!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Selecciona las plataformas de streaming que tienes para personalizar tus recomendaciones",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Grid de plataformas con animación
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                            slideInVertically(initialOffsetY = { 100 })
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Mis plataformas:",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            items(PlataformasCatalogo.TODAS) { plataforma ->
                                ModernPlataformaCard(
                                    plataforma = plataforma,
                                    seleccionada = state.plataformasSeleccionadas.contains(plataforma.id),
                                    onToggle = { vm.togglePlataforma(plataforma.id) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Mensaje de error
                state.error?.let { error ->
                    ModernMessageCard(
                        message = error,
                        isError = true
                    )
                }
            }

            // Botón flotante en la parte inferior
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 300)) +
                        slideInVertically(initialOffsetY = { it })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f),
                                    Color.Black
                                )
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    ModernButton(
                        onClick = { vm.guardarConfiguracion() },
                        text = "Continuar",
                        enabled = state.plataformasSeleccionadas.isNotEmpty() && !state.guardando,
                        loading = state.guardando,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ModernPlataformaCard(
    plataforma: PlataformaStreaming,
    seleccionada: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(
                elevation = if (seleccionada) 8.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (seleccionada) GlowTeal else Color.Black.copy(alpha = 0.3f),
                spotColor = if (seleccionada) GlowTeal else Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (seleccionada)
                    Brush.verticalGradient(
                        colors = listOf(
                            TealPastel.copy(alpha = 0.3f),
                            TealDark.copy(alpha = 0.2f)
                        )
                    )
                else
                    Brush.verticalGradient(
                        colors = listOf(
                            SurfaceLight.copy(alpha = 0.8f),
                            SurfaceDark.copy(alpha = 0.6f)
                        )
                    )
            )
            .border(
                width = if (seleccionada) 2.dp else 0.dp,
                color = if (seleccionada) TealPastel else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onToggle)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = plataforma.icono),
            contentDescription = plataforma.nombre,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentScale = ContentScale.Fit
        )

        if (seleccionada) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seleccionado",
                tint = TealPastel,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            )
        }
    }
}

// Mantener compatibilidad
@Composable
fun PlataformaCard(
    plataforma: PlataformaStreaming,
    seleccionada: Boolean,
    onToggle: () -> Unit
) {
    ModernPlataformaCard(plataforma, seleccionada, onToggle)
}