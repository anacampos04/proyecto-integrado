package com.example.anacampospi.ui.componentes

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.anacampospi.modelo.ContenidoLite
import com.example.anacampospi.modelo.PlataformasCatalogo
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Tarjeta swipeable con capacidad de voltear para mostrar información adicional
 */
@Composable
fun FlippableSwipeCard(
    contenido: ContenidoLite,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier,
    onLoadDetails: ((ContenidoLite) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estados de animación
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    var isFlipped by remember { mutableStateOf(false) }
    val flipRotation = remember { Animatable(0f) }
    var hasLoadedDetails by remember { mutableStateOf(false) }

    // Umbral para considerar un swipe válido
    val swipeThreshold = 300f

    // Calcular la dirección y transparencia de los overlays
    val swipeProgress = (offsetX.value / swipeThreshold).coerceIn(-1f, 1f)
    val likeAlpha = if (swipeProgress > 0) swipeProgress else 0f
    val dislikeAlpha = if (swipeProgress < 0) -swipeProgress else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.65f)
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                rotationZ = rotation.value
                rotationY = flipRotation.value
                cameraDistance = 12f * density
            }
            .pointerInput(isFlipped) { // Reiniciar cuando cambia isFlipped
                // Solo permitir swipe cuando NO está volteada
                if (!isFlipped) {
                    detectDragGestures(
                        onDragEnd = {
                            scope.launch {
                                // Si se pasó el umbral, ejecutar la acción
                                if (abs(offsetX.value) > swipeThreshold) {
                                    // Animación de salida
                                    val targetX = if (offsetX.value > 0) 1000f else -1000f
                                    launch { offsetX.animateTo(targetX, tween(300)) }
                                    launch { offsetY.animateTo(300f, tween(300)) }
                                    launch {
                                        rotation.animateTo(
                                            if (offsetX.value > 0) 30f else -30f,
                                            tween(300)
                                        )
                                    }

                                    // Ejecutar callback
                                    kotlinx.coroutines.delay(200)
                                    if (offsetX.value > 0) {
                                        onSwipeRight()
                                    } else {
                                        onSwipeLeft()
                                    }
                                } else {
                                    // Regresar a la posición original
                                    launch { offsetX.animateTo(0f, tween(300)) }
                                    launch { offsetY.animateTo(0f, tween(300)) }
                                    launch { rotation.animateTo(0f, tween(300)) }
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                offsetY.snapTo(offsetY.value + dragAmount.y * 0.3f)
                                // Rotación proporcional al desplazamiento
                                rotation.snapTo((offsetX.value / 20f).coerceIn(-15f, 15f))
                            }
                        }
                    )
                }
            }
            .clickable {
                // Voltear la tarjeta al hacer clic
                scope.launch {
                    isFlipped = !isFlipped
                    flipRotation.animateTo(
                        if (isFlipped) 180f else 0f,
                        tween(600)
                    )

                    // Cargar detalles bajo demanda solo la primera vez que se voltea
                    if (isFlipped && !hasLoadedDetails && onLoadDetails != null) {
                        hasLoadedDetails = true
                        onLoadDetails(contenido)
                    }
                }
            }
    ) {
        // Determinar qué cara mostrar según la rotación
        val showFront = flipRotation.value < 90f

        if (showFront) {
            // Cara frontal: Poster
            CardFront(
                contenido = contenido,
                likeAlpha = likeAlpha,
                dislikeAlpha = dislikeAlpha
            )
        } else {
            // Cara trasera: Información detallada
            CardBack(
                contenido = contenido,
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f // Corregir inversión horizontal
                },
                onPlayTrailer = { trailerKey ->
                    // Abrir trailer en YouTube
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=$trailerKey")
                    )
                    context.startActivity(intent)
                }
            )
        }
    }
}

/**
 * Cara frontal de la tarjeta con el poster
 */
@Composable
private fun CardFront(
    contenido: ContenidoLite,
    likeAlpha: Float,
    dislikeAlpha: Float
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo (poster)
            AsyncImage(
                model = contenido.posterUrl,
                contentDescription = contenido.titulo,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradiente oscuro en la parte inferior
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 400f
                        )
                    )
            )

            // Información del contenido
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = contenido.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Año
                    if (contenido.anioEstreno > 0) {
                        Text(
                            text = contenido.anioEstreno.toString(),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp
                        )
                    }

                    // Tipo
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = contenido.tipo.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Puntuación
                    if (contenido.puntuacion > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⭐",
                                fontSize = 14.sp
                            )
                            Text(
                                text = String.format("%.1f", contenido.puntuacion),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            // Icono de volteo en la esquina inferior derecha
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = "Ver más información",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }

            // Overlay de "LIKE"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(likeAlpha)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Me gusta",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "ME GUSTA",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Overlay de "DISLIKE"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(dislikeAlpha)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "No me gusta",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "NO ME GUSTA",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cara trasera de la tarjeta con información detallada
 */
@Composable
private fun CardBack(
    contenido: ContenidoLite,
    modifier: Modifier = Modifier,
    onPlayTrailer: (String) -> Unit
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Poster de fondo con opacidad reducida
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.3f)
            ) {
                AsyncImage(
                    model = contenido.posterUrl,
                    contentDescription = contenido.titulo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Fondo negro semi-transparente para mejorar legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Contenido scrolleable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Título
                Text(
                    text = contenido.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Géneros
                if (contenido.generos.isNotEmpty()) {
                    Text(
                        text = contenido.generos.joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Sinopsis
                if (!contenido.sinopsis.isNullOrBlank()) {
                    Text(
                        text = contenido.sinopsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Justify
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Plataformas disponibles con logos
                if (contenido.proveedores.isNotEmpty()) {
                    Text(
                        text = "Disponible en",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    val plataformas = PlataformasCatalogo.getPlataformas(contenido.proveedores)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        plataformas.take(6).forEach { plataforma ->
                            Image(
                                painter = painterResource(id = plataforma.icono),
                                contentDescription = plataforma.nombre,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(4.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        if (plataformas.size > 6) {
                            Text(
                                text = "+${plataformas.size - 6}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Trailer
                if (contenido.trailer != null) {
                    Text(
                        text = "Trailer",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Miniatura del trailer clicable
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .clickable { onPlayTrailer(contenido.trailer.key) }
                    ) {
                        // Thumbnail del trailer de YouTube
                        AsyncImage(
                            model = contenido.trailer.thumbnailUrl,
                            contentDescription = "Trailer thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Overlay oscuro con botón play
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = Color.Red,
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Icono de volver en la esquina inferior derecha
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Volver al frente",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }
        }
    }
}
