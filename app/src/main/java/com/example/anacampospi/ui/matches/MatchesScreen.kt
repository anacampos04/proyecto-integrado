package com.example.anacampospi.ui.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anacampospi.BuildConfig
import com.example.anacampospi.data.tmdb.TmdbClient
import com.example.anacampospi.ui.componentes.ContentDetailModal
import com.example.anacampospi.ui.componentes.ContentDetails
import com.example.anacampospi.ui.componentes.MatchCard
import com.example.anacampospi.viewModels.FiltroTipo
import com.example.anacampospi.viewModels.MatchesViewModel

/**
 * Pantalla de matches (contenido que coincide con amigos).
 * Soporta dos modos:
 * - Modo general (grupoId = null): muestra matches de todos los grupos con filtros
 * - Modo específico (grupoId != null): muestra solo matches de ese grupo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    grupoId: String? = null,
    grupoNombre: String? = null,
    grupoIdInicial: String? = null, // Pre-selecciona este grupo en el filtro (modo general)
    viewModel: MatchesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estado del modal de detalles
    var selectedContent by remember { mutableStateOf<ContentDetails?>(null) }
    val tmdbRepository = remember { TmdbClient.createRepository(BuildConfig.TMDB_API_KEY) }

    // Cargar matches al iniciar
    LaunchedEffect(grupoId, grupoIdInicial) {
        viewModel.cargarMatches(grupoId = grupoId, grupoIdInicial = grupoIdInicial)
    }

    // Modal de detalles
    selectedContent?.let { content ->
        ContentDetailModal(
            contentDetails = content,
            onDismiss = { selectedContent = null },
            tmdbRepository = tmdbRepository
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (grupoId != null && grupoNombre != null) {
                            "Matches: $grupoNombre"
                        } else {
                            "Mis Matches"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.loading -> {
                    // Estado de carga
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Cargando matches...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                uiState.error != null -> {
                    // Estado de error
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Error al cargar matches",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.error ?: "Error desconocido",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.cargarMatches(grupoId) }
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Reintentar")
                            }
                        }
                    }
                }

                uiState.matchesVacios -> {
                    // Sin matches en absoluto
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "No hay matches aún",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.modoGrupoEspecifico) {
                                    "Empieza a votar contenido con tu grupo para ver tus coincidencias aquí"
                                } else {
                                    "Crea una ronda y vota contenido con tus amigos para hacer Match"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                uiState.filtrosVacios -> {
                    // Hay matches pero los filtros no devuelven resultados
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Filtros de grupos (solo en modo general)
                        if (!uiState.modoGrupoEspecifico && uiState.gruposDisponibles.isNotEmpty()) {
                            FiltrosGrupoChips(
                                grupos = uiState.gruposDisponibles,
                                grupoSeleccionado = uiState.grupoSeleccionado,
                                onGrupoSeleccionado = { viewModel.seleccionarGrupo(it) }
                            )
                        }

                        // Pestañas de filtro de tipo
                        FiltrosTipoPestanas(
                            filtroActual = uiState.filtroTipo,
                            onFiltroSeleccionado = { viewModel.cambiarFiltroTipo(it) }
                        )

                        // Mensaje de filtros vacíos
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = when {
                                        uiState.filtroTipo == FiltroTipo.PELICULAS -> "No hay matches de películas"
                                        uiState.filtroTipo == FiltroTipo.SERIES -> "No hay matches de series"
                                        uiState.grupoSeleccionado != null -> {
                                            val nombreGrupo = uiState.gruposDisponibles.find { it.id == uiState.grupoSeleccionado }?.nombre
                                            "No hay matches en $nombreGrupo"
                                        }
                                        else -> "No hay matches con este filtro"
                                    },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Prueba con otro filtro para ver más matches",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                else -> {
                    // Contenido principal con matches
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Filtros de grupos (solo en modo general)
                        if (!uiState.modoGrupoEspecifico && uiState.gruposDisponibles.isNotEmpty()) {
                            FiltrosGrupoChips(
                                grupos = uiState.gruposDisponibles,
                                grupoSeleccionado = uiState.grupoSeleccionado,
                                onGrupoSeleccionado = { viewModel.seleccionarGrupo(it) }
                            )
                        }

                        // Pestañas de filtro de tipo
                        FiltrosTipoPestanas(
                            filtroActual = uiState.filtroTipo,
                            onFiltroSeleccionado = { viewModel.cambiarFiltroTipo(it) }
                        )

                        // Lista de matches
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.matches,
                                key = { "${it.grupoId}_${it.match.idContenido}" }
                            ) { matchData ->
                                MatchCard(
                                    matchData = matchData,
                                    mostrarGrupo = !uiState.modoGrupoEspecifico,
                                    onClick = {
                                        // Abrir modal con detalles del match
                                        selectedContent = ContentDetails(
                                            idContenido = matchData.match.idContenido,
                                            titulo = matchData.match.titulo,
                                            posterUrl = matchData.match.posterUrl,
                                            tipo = matchData.match.tipo,
                                            anioEstreno = matchData.match.anioEstreno,
                                            puntuacion = matchData.match.puntuacion,
                                            proveedores = matchData.match.proveedores,
                                            plataformasGrupo = matchData.plataformasGrupo,
                                            sinopsis = null, // Se cargará en el modal
                                            trailerKey = null // Se cargará en el modal
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chips horizontales para filtrar por grupo (modo general).
 */
@Composable
private fun FiltrosGrupoChips(
    grupos: List<com.example.anacampospi.viewModels.GrupoInfo>,
    grupoSeleccionado: String?,
    onGrupoSeleccionado: (String?) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chip "Todos" sin icono check y más redondeado
        FilterChip(
            selected = grupoSeleccionado == null,
            onClick = { onGrupoSeleccionado(null) },
            label = { Text("Todos") },
            shape = RoundedCornerShape(20.dp)
        )

        // Chips de grupos sin icono check y más redondeados
        grupos.forEach { grupo ->
            FilterChip(
                selected = grupoSeleccionado == grupo.id,
                onClick = { onGrupoSeleccionado(grupo.id) },
                label = { Text(grupo.nombre) },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

/**
 * Pestañas para filtrar por tipo de contenido.
 */
@Composable
private fun FiltrosTipoPestanas(
    filtroActual: FiltroTipo,
    onFiltroSeleccionado: (FiltroTipo) -> Unit
) {
    val opciones = listOf(
        FiltroTipo.TODOS to "Todos",
        FiltroTipo.PELICULAS to "Películas",
        FiltroTipo.SERIES to "Series"
    )

    ScrollableTabRow(
        selectedTabIndex = opciones.indexOfFirst { it.first == filtroActual },
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 16.dp,
        divider = {}
    ) {
        opciones.forEach { (filtro, label) ->
            Tab(
                selected = filtroActual == filtro,
                onClick = { onFiltroSeleccionado(filtro) },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (filtro) {
                            FiltroTipo.TODOS -> {
                                Icon(
                                    imageVector = Icons.Outlined.Movie,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Tv,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            FiltroTipo.PELICULAS -> Icon(
                                imageVector = Icons.Outlined.Movie,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            FiltroTipo.SERIES -> Icon(
                                imageVector = Icons.Outlined.Tv,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(label)
                    }
                }
            )
        }
    }
}
