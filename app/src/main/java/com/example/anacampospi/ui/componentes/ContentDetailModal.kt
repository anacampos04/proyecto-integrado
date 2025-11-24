package com.example.anacampospi.ui.componentes

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.anacampospi.data.tmdb.TmdbRepository
import com.example.anacampospi.modelo.PlataformasCatalogo
import com.example.anacampospi.modelo.enums.TipoContenido
import com.example.anacampospi.ui.theme.TealPastel
import kotlinx.coroutines.launch

/**
 * Datos del contenido para mostrar en el modal
 */
data class ContentDetails(
    val idContenido: String,
    val titulo: String,
    val posterUrl: String,
    val tipo: TipoContenido,
    val anioEstreno: Int,
    val puntuacion: Double,
    val proveedores: List<String>,
    val plataformasGrupo: List<String> = emptyList(), // Plataformas seleccionadas por el grupo
    val sinopsis: String? = null,
    val trailerKey: String? = null
)

/**
 * Modal moderno para mostrar detalles completos del contenido
 * Incluye sinopsis, trailer y metadata
 */
@Composable
fun ContentDetailModal(
    contentDetails: ContentDetails,
    onDismiss: () -> Unit,
    tmdbRepository: TmdbRepository
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var detallesCompletos by remember { mutableStateOf(contentDetails) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cargar detalles completos al abrir el modal
    LaunchedEffect(contentDetails.idContenido) {
        scope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Extraer ID numérico del formato "movie:123" o "tv:456"
                val contentId = contentDetails.idContenido.substringAfter(":").toIntOrNull()

                if (contentId != null) {
                    when (contentDetails.tipo) {
                        TipoContenido.PELICULA -> {
                            val movieResult = tmdbRepository.getMovieDetails(contentId)
                            movieResult.onSuccess { movie ->
                                val trailerResult = tmdbRepository.enrichContentDetails(
                                    com.example.anacampospi.modelo.ContenidoLite(
                                        idContenido = contentDetails.idContenido,
                                        tipo = TipoContenido.PELICULA
                                    )
                                )

                                trailerResult.onSuccess { enriched ->
                                    detallesCompletos = contentDetails.copy(
                                        sinopsis = movie.overview,
                                        trailerKey = enriched.trailer?.key
                                    )
                                }
                            }.onFailure {
                                errorMessage = "Error al cargar detalles"
                            }
                        }
                        TipoContenido.SERIE -> {
                            val tvResult = tmdbRepository.getTvShowDetails(contentId)
                            tvResult.onSuccess { tvShow ->
                                val trailerResult = tmdbRepository.enrichContentDetails(
                                    com.example.anacampospi.modelo.ContenidoLite(
                                        idContenido = contentDetails.idContenido,
                                        tipo = TipoContenido.SERIE
                                    )
                                )

                                trailerResult.onSuccess { enriched ->
                                    detallesCompletos = contentDetails.copy(
                                        sinopsis = tvShow.overview,
                                        trailerKey = enriched.trailer?.key
                                    )
                                }
                            }.onFailure {
                                errorMessage = "Error al cargar detalles"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header con poster y gradiente
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        // Poster de fondo
                        if (detallesCompletos.posterUrl.isNotBlank()) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w780${detallesCompletos.posterUrl}",
                                contentDescription = "Poster de ${detallesCompletos.titulo}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Gradiente oscuro para legibilidad
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Black.copy(alpha = 0.8f)
                                        )
                                    )
                                )
                        )

                        // Botón de cerrar
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }

                        // Información básica en la parte inferior del poster
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Badge de tipo
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (detallesCompletos.tipo) {
                                    TipoContenido.PELICULA -> MaterialTheme.colorScheme.primary
                                    TipoContenido.SERIE -> MaterialTheme.colorScheme.secondary
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (detallesCompletos.tipo) {
                                            TipoContenido.PELICULA -> Icons.Outlined.Movie
                                            TipoContenido.SERIE -> Icons.Outlined.Tv
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = when (detallesCompletos.tipo) {
                                            TipoContenido.PELICULA -> "Película"
                                            TipoContenido.SERIE -> "Serie"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            // Título
                            Text(
                                text = detallesCompletos.titulo,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Año y puntuación
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (detallesCompletos.anioEstreno > 0) {
                                    Text(
                                        text = detallesCompletos.anioEstreno.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }

                                if (detallesCompletos.puntuacion > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = Color(0xFFFFD700)
                                        )
                                        Text(
                                            text = String.format("%.1f", detallesCompletos.puntuacion),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Contenido
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Estado de carga
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        // Error
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Trailer
                        if (!detallesCompletos.trailerKey.isNullOrBlank()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Trailer",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://www.youtube.com/watch?v=${detallesCompletos.trailerKey}")
                                        )
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TealPastel, // Azul del tema
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Black
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Ver trailer",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        // Sinopsis
                        if (!detallesCompletos.sinopsis.isNullOrBlank()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Sinopsis",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = detallesCompletos.sinopsis!!,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Plataformas disponibles (desde el match guardado en Firestore)
                        if (detallesCompletos.proveedores.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Disponible en",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                val plataformas = PlataformasCatalogo.getPlataformas(detallesCompletos.proveedores)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    plataformas.forEach { plataforma ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                androidx.compose.foundation.Image(
                                                    painter = painterResource(id = plataforma.icono),
                                                    contentDescription = "Logo ${plataforma.nombre}",
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(4.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            Text(
                                                text = plataforma.nombre,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2
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
    }
}
