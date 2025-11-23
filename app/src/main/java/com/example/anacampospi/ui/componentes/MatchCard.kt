package com.example.anacampospi.ui.componentes

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.anacampospi.R
import com.example.anacampospi.modelo.PlataformasCatalogo
import com.example.anacampospi.modelo.Usuario
import com.example.anacampospi.modelo.enums.TipoContenido
import com.example.anacampospi.viewModels.MatchConGrupoConUsuarios

/**
 * Tarjeta que muestra un match de contenido.
 * Incluye poster, información básica, grupo, usuarios coincidentes y proveedores.
 */
@Composable
fun MatchCard(
    matchData: MatchConGrupoConUsuarios,
    mostrarGrupo: Boolean = true, // False en modo grupo específico
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val match = matchData.match

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Poster a la izquierda
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
            ) {
                if (match.posterUrl.isNotBlank()) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w342${match.posterUrl}",
                        contentDescription = "Poster de ${match.titulo}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder si no hay poster
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                // Badge de tipo (película/serie) en esquina superior izquierda
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = when (match.tipo) {
                        TipoContenido.PELICULA -> MaterialTheme.colorScheme.primary
                        TipoContenido.SERIE -> MaterialTheme.colorScheme.secondary
                    }
                ) {
                    Text(
                        text = when (match.tipo) {
                            TipoContenido.PELICULA -> "🎬"
                            TipoContenido.SERIE -> "📺"
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        fontSize = 14.sp
                    )
                }
            }

            // Información a la derecha
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Título y año
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = match.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (match.anioEstreno > 0) {
                            Text(
                                text = match.anioEstreno.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (match.puntuacion > 0) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFFD700) // Dorado
                                )
                                Text(
                                    text = String.format("%.1f", match.puntuacion),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Badge del grupo (solo en modo general)
                    if (mostrarGrupo) {
                        Surface(
                            modifier = Modifier.padding(top = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = matchData.grupoNombre,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Usuarios coincidentes y proveedores
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Usuarios coincidentes
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👥",
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (matchData.usuariosInfo.isNotEmpty()) {
                                val nombres = matchData.usuariosInfo.take(3).map { it.nombre }
                                val texto = nombres.joinToString(", ")
                                if (matchData.usuariosInfo.size > 3) {
                                    "$texto +${matchData.usuariosInfo.size - 3}"
                                } else {
                                    texto
                                }
                            } else {
                                "${match.usuariosCoincidentes.size} usuarios"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Proveedores disponibles
                    if (match.proveedores.isNotEmpty()) {
                        val plataformas = PlataformasCatalogo.getPlataformas(match.proveedores)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            plataformas.take(4).forEach { plataforma ->
                                Surface(
                                    modifier = Modifier.size(24.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(id = plataforma.icono),
                                        contentDescription = "Logo ${plataforma.nombre}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(2.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }

                            if (plataformas.size > 4) {
                                Text(
                                    text = "+${plataformas.size - 4}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
