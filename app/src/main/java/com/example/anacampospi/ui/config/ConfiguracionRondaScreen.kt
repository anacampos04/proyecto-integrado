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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anacampospi.data.tmdb.models.TmdbGenres
import com.example.anacampospi.modelo.PlataformasCatalogo
import com.example.anacampospi.modelo.Usuario
import com.example.anacampospi.modelo.enums.TipoContenido
import com.example.anacampospi.ui.componentes.*
import com.example.anacampospi.ui.theme.*
import com.example.anacampospi.viewModels.ConfiguracionRondaViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de configuración de ronda de swipe.
 * Soporta dos modos:
 * - Creador (grupoId = null): configurar nueva ronda con amigos, tipos, plataformas y géneros
 * - Invitado (grupoId != null): configurar solo plataformas y géneros en ronda existente
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConfiguracionRondaScreen(
    grupoId: String? = null, // Si es null, modo creador. Si no, modo invitado.
    onIniciarRonda: (grupoId: String, irASwipes: Boolean) -> Unit,
    onBack: () -> Unit = {},
    viewModel: ConfiguracionRondaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var nombreRonda by remember { mutableStateOf("") }
    var amigosSeleccionados by remember { mutableStateOf(setOf<String>()) }
    var plataformasSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    var peliculasEnabled by remember { mutableStateOf(true) }
    var seriesEnabled by remember { mutableStateOf(true) }
    var generosSeleccionados by remember { mutableStateOf(setOf<Int>()) }

    // Inicializar ViewModel según el modo
    LaunchedEffect(grupoId) {
        viewModel.inicializar(grupoId)
    }

    // Pre-seleccionar las plataformas del usuario cuando se carguen
    LaunchedEffect(uiState.plataformasUsuario) {
        if (uiState.plataformasUsuario.isNotEmpty() && plataformasSeleccionadas.isEmpty()) {
            plataformasSeleccionadas = uiState.plataformasUsuario.toSet()
        }
    }

    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    // Determinar tipos de contenido basado en switches
    // El creador puede elegir: solo películas, solo series, o ambos
    val tiposSeleccionados = buildList {
        if (peliculasEnabled) add(TipoContenido.PELICULA)
        if (seriesEnabled) add(TipoContenido.SERIE)
    }.takeIf { it.isNotEmpty() } ?: listOf(TipoContenido.PELICULA) // Por defecto películas si ninguno está activo

    // Validar que el botón pueda estar habilitado
    val puedeIniciarRonda = if (uiState.esInvitado) {
        // Invitado: solo necesita plataformas (géneros son opcionales)
        plataformasSeleccionadas.isNotEmpty()
    } else {
        // Creador: necesita amigos y plataformas (géneros son opcionales)
        amigosSeleccionados.isNotEmpty() && plataformasSeleccionadas.isNotEmpty()
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
                    Column {
                        Text(
                            text = if (uiState.esInvitado) "Configurar mis preferencias" else "Preparativos de la fiesta",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (uiState.esInvitado && uiState.grupo != null) {
                            Text(
                                text = uiState.grupo!!.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
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
                // Sección de nombre personalizado (solo para creador)
                if (!uiState.esInvitado) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 50)) +
                                slideInVertically(initialOffsetY = { 50 })
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Nombre de la fiesta",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Elige un nombre único para esta sesión",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )

                            OutlinedTextField(
                                value = nombreRonda,
                                onValueChange = { if (it.length <= 30) nombreRonda = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Noches de sofá, Pijama party...") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealPastel,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = TealPastel,
                                    focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Divider
                    AnimatedVisibility(visible = visible) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }

                // Sección de selección de amigos (solo para creador)
                if (!uiState.esInvitado) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 100)) +
                                slideInVertically(initialOffsetY = { 50 })
                    ) {
                        SelectorAmigosSection(
                            amigos = uiState.amigos,
                            amigosSeleccionados = amigosSeleccionados,
                            onAmigoToggle = { uid ->
                                amigosSeleccionados = if (amigosSeleccionados.contains(uid)) {
                                    amigosSeleccionados - uid
                                } else {
                                    amigosSeleccionados + uid
                                }
                            },
                            cargando = uiState.cargandoAmigos
                        )
                    }

                    // Divider
                    AnimatedVisibility(visible = visible) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }

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
                            text = if (uiState.esInvitado)
                                "Tus plataformas están pre-seleccionadas, pero puedes cambiarlas"
                            else
                                "Tus plataformas están pre-seleccionadas, pero puedes cambiarlas",
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

                // Sección de tipo de contenido con SWITCHES (solo para creador)
                if (!uiState.esInvitado) {
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
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Movie,
                                                    contentDescription = null,
                                                    tint = TealPastel,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "Películas",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White
                                                )
                                            }
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
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Tv,
                                                    contentDescription = null,
                                                    tint = TealPastel,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "Series",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White
                                                )
                                            }
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

                // Mensaje informativo
                if (!uiState.esInvitado && amigosSeleccionados.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 350))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = TealPastel.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = TealPastel,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Tus amigos recibirán la invitación",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Podrás hacer swipe cuando todos configuren sus preferencias",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Mensaje de validación si no puede iniciar
                        if (!puedeIniciarRonda && !uiState.esInvitado) {
                            if (amigosSeleccionados.isEmpty()) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 32.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = PopcornYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Debes seleccionar al menos un amigo para crear la ronda",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PopcornYellow,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else if (plataformasSeleccionadas.isEmpty()) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 32.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = PopcornYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Debes seleccionar al menos una plataforma",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PopcornYellow,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Botón circular con icono
                        FloatingActionButton(
                            onClick = {
                                if (!puedeIniciarRonda || uiState.creandoGrupo) return@FloatingActionButton

                                scope.launch {
                                    if (uiState.esInvitado && grupoId != null) {
                                        // Modo invitado: configurar ronda existente
                                        val result = viewModel.configurarRonda(
                                            idGrupo = grupoId,
                                            plataformas = plataformasSeleccionadas.toList(),
                                            generos = generosSeleccionados.toList()
                                        )
                                        if (result.isSuccess) {
                                            // Verificar si el grupo quedó ACTIVO (último en configurar)
                                            val grupoActualizado = viewModel.verificarEstadoGrupo(grupoId)
                                            val irASwipes = grupoActualizado?.estado == "ACTIVA"
                                            onIniciarRonda(grupoId, irASwipes)
                                        }
                                    } else {
                                        // Modo creador: crear nueva ronda (siempre va a home a esperar)
                                        val nuevoGrupoId = viewModel.crearGrupo(
                                            nombrePersonalizado = nombreRonda.trim().ifBlank { null },
                                            amigosSeleccionados = amigosSeleccionados.toList(),
                                            plataformas = plataformasSeleccionadas.toList(),
                                            tipos = tiposSeleccionados,
                                            generos = generosSeleccionados.toList()
                                        )
                                        nuevoGrupoId?.let { onIniciarRonda(it, false) }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = CircleShape,
                                    ambientColor = GlowTeal,
                                    spotColor = GlowTeal
                                ),
                            containerColor = if (puedeIniciarRonda && !uiState.creandoGrupo) TealPastel else Color.Gray,
                            contentColor = Color.Black,
                            shape = CircleShape
                        ) {
                            if (uiState.creandoGrupo) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = Color.Black
                                )
                            } else {
                                Icon(
                                    imageVector = if (uiState.esInvitado) Icons.Default.Check else Icons.Default.PlayArrow,
                                    contentDescription = if (uiState.esInvitado) "Guardar configuración" else "Iniciar ronda",
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
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

/**
 * Sección de selección de amigos para la ronda
 */
@Composable
fun SelectorAmigosSection(
    amigos: List<Usuario>,
    amigosSeleccionados: Set<String>,
    onAmigoToggle: (String) -> Unit,
    cargando: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Invitar amigos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Selecciona los amigos que participarán en esta ronda",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PopcornYellow)
            }
        } else if (amigos.isEmpty()) {
            // Estado vacío
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceLight.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tienes amigos aún",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Añade amigos desde la pestaña Amigos",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Lista de amigos
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                amigos.forEach { amigo ->
                    AmigoCheckboxItem(
                        amigo = amigo,
                        isSelected = amigosSeleccionados.contains(amigo.idUsuario),
                        onToggle = { onAmigoToggle(amigo.idUsuario) }
                    )
                }
            }
        }
    }
}

/**
 * Item de amigo con checkbox
 */
@Composable
fun AmigoCheckboxItem(
    amigo: Usuario,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PopcornYellow.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = PopcornYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = amigo.nombre.ifBlank { "Usuario sin nombre" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = amigo.codigoInvitacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = TealPastel,
                    uncheckedColor = Color.White.copy(alpha = 0.5f),
                    checkmarkColor = Color.Black
                )
            )
        }
    }
}
