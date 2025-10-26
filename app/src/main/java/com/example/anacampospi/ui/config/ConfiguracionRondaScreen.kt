package com.example.anacampospi.ui.config

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.anacampospi.data.tmdb.models.TmdbGenres
import com.example.anacampospi.modelo.PlataformasCatalogo
import com.example.anacampospi.modelo.enums.TipoContenido
import com.example.anacampospi.ui.componentes.*
import com.example.anacampospi.ui.theme.*

/**
 * Pantalla de configuración de ronda de swipe
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConfiguracionRondaScreen(
    onIniciarRonda: (plataformas: List<String>, tipo: TipoContenido?, generos: List<Int>) -> Unit,
    onBack: () -> Unit = {}
) {
    var plataformasSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    var peliculasEnabled by remember { mutableStateOf(true) }
    var seriesEnabled by remember { mutableStateOf(true) }
    var generosSeleccionados by remember { mutableStateOf(setOf<Int>()) }

    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    // Determinar tipo de contenido basado en switches
    val tipoSeleccionado = when {
        peliculasEnabled && seriesEnabled -> null // Ambos
        peliculasEnabled -> TipoContenido.PELICULA
        seriesEnabled -> TipoContenido.SERIE
        else -> null // Si ambos están desactivados, permitir ambos por defecto
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
            // Top bar moderna
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { -it })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Configurar ronda",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Contenido principal con scroll
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // Sección de plataformas
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 100)) +
                            slideInVertically(initialOffsetY = { 50 })
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Plataformas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Selecciona al menos una plataforma",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid de plataformas sin scroll interno - 3 columnas
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            maxItemsInEachRow = 3
                        ) {
                            PlataformasCatalogo.TODAS.forEach { plataforma ->
                                ModernPlataformaItem(
                                    plataforma = plataforma,
                                    isSelected = plataformasSeleccionadas.contains(plataforma.id),
                                    onClick = {
                                        plataformasSeleccionadas = if (plataformasSeleccionadas.contains(plataforma.id)) {
                                            plataformasSeleccionadas - plataforma.id
                                        } else {
                                            plataformasSeleccionadas + plataforma.id
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Divider moderno
                AnimatedVisibility(visible = visible) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color.White.copy(alpha = 0.1f)
                    )
                }

                // Sección de tipo de contenido con SWITCHES
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                            slideInVertically(initialOffsetY = { 50 })
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Tipo de contenido",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Activa los tipos que quieras ver",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = TealPastel.copy(alpha = 0.2f)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceLight.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Switch para películas
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🎬 Películas",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Incluir películas en la ronda",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                    Switch(
                                        checked = peliculasEnabled,
                                        onCheckedChange = { peliculasEnabled = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.Black,
                                            checkedTrackColor = TealPastel,
                                            uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                            uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                                        )
                                    )
                                }

                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = Color.White.copy(alpha = 0.1f)
                                )

                                // Switch para series
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "📺 Series",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Incluir series en la ronda",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                    Switch(
                                        checked = seriesEnabled,
                                        onCheckedChange = { seriesEnabled = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.Black,
                                            checkedTrackColor = TealPastel,
                                            uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                            uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Divider moderno
                AnimatedVisibility(visible = visible) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color.White.copy(alpha = 0.1f)
                    )
                }

                // Sección de géneros
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 300)) +
                            slideInVertically(initialOffsetY = { 50 })
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Géneros (opcional)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Filtra por tus géneros favoritos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        // Grid de géneros sin scroll interno - 2 columnas
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            maxItemsInEachRow = 2
                        ) {
                            TmdbGenres.getAllGenres().forEach { (id, nombre) ->
                                ModernGeneroChip(
                                    nombre = nombre,
                                    isSelected = generosSeleccionados.contains(id),
                                    onClick = {
                                        generosSeleccionados = if (generosSeleccionados.contains(id)) {
                                            generosSeleccionados - id
                                        } else {
                                            generosSeleccionados + id
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Botón al final del contenido (dentro del scroll)
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) +
                            slideInVertically(initialOffsetY = { 100 })
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Botón circular con icono de play
                        FloatingActionButton(
                            onClick = {
                                onIniciarRonda(
                                    plataformasSeleccionadas.toList(),
                                    tipoSeleccionado,
                                    generosSeleccionados.toList()
                                )
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = CircleShape,
                                    ambientColor = GlowTeal,
                                    spotColor = GlowTeal
                                ),
                            containerColor = TealPastel,
                            contentColor = Color.Black,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Iniciar ronda",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernPlataformaItem(
    plataforma: com.example.anacampospi.modelo.PlataformaStreaming,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = if (isSelected) 8.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isSelected) GlowTeal else Color.Black.copy(alpha = 0.3f),
                spotColor = if (isSelected) GlowTeal else Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected)
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
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) TealPastel else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
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

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seleccionado",
                tint = TealPastel,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
private fun ModernGeneroChip(
    nombre: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isSelected) 4.dp else 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = if (isSelected) TealPastel.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                TealPastel.copy(alpha = 0.2f)
            else
                SurfaceLight.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = TealPastel,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Mantener los componentes antiguos por compatibilidad pero delegando a los nuevos
@Composable
private fun PlataformaItem(
    plataforma: com.example.anacampospi.modelo.PlataformaStreaming,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ModernPlataformaItem(plataforma, isSelected, onClick)
}

@Composable
private fun GeneroChip(
    nombre: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ModernGeneroChip(nombre, isSelected, onClick)
}
