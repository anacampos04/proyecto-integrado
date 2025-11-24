package com.example.anacampospi.ui.swipe

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.anacampospi.R
import com.example.anacampospi.modelo.enums.TipoContenido
import com.example.anacampospi.ui.componentes.FlippableSwipeCard
import com.example.anacampospi.ui.theme.*
import com.example.anacampospi.viewModels.SwipeViewModel
import com.airbnb.lottie.RenderMode

/**
 * Pantalla principal de swipe para votar contenido.
 * Soporta dos modos:
 * - Con grupoId: carga los filtros combinados del grupo
 * - Sin grupoId: usa filtros manuales (compatibilidad)
 */
@Composable
fun SwipeScreen(
    grupoId: String? = null,
    plataformas: List<String>? = null,
    tipo: TipoContenido? = null,
    generos: List<Int>? = null,
    onBack: () -> Unit = {},
    onVerMatches: () -> Unit = {},
    viewModel: SwipeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Cargar contenido según el modo
    LaunchedEffect(grupoId, plataformas, tipo, generos) {
        if (grupoId != null) {
            // Modo grupo: usar filtros combinados del grupo
            viewModel.inicializarConGrupo(grupoId)
        } else {
            // Modo manual: usar filtros pasados directamente
            viewModel.cargarContenido(
                tipo = tipo,
                plataformas = plataformas,
                generos = generos
            )
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
        when {
            // Esperando a otros usuarios (ronda no activa)
            uiState.esperandoOtrosUsuarios -> {
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
                            imageVector = Icons.Rounded.HourglassEmpty,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = TealPastel
                        )
                        Text(
                            "Esperando al grupo",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "La fiesta aún no está lista. \n Algunos amigos todavía tienen que configurar sus preferencias.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        // Mostrar nombres de usuarios pendientes
                        if (uiState.usuariosPendientes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(0.9f),
                                colors = CardDefaults.cardColors(
                                    containerColor = SurfaceLight.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Esperando a:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PopcornYellow,
                                        fontWeight = FontWeight.Bold
                                    )
                                    uiState.usuariosPendientes.forEach { usuario ->
                                        Text(
                                            text = "• ${usuario.nombre.ifBlank { "Usuario sin nombre" }}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón para compartir por WhatsApp
                        Button(
                            onClick = {
                                val mensaje = "¡Hola! 👋\n\n" +
                                        "Estoy esperando que configures tus preferencias para empezar nuestra ronda de votación en PopCornTribu.\n\n" +
                                        "¡Entra a la app para que podamos empezar! 🎬🍿"

                                try {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.data =
                                        Uri.parse("https://wa.me/?text=${Uri.encode(mensaje)}")
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Si WhatsApp no está instalado, compartir de forma genérica
                                    val intent = Intent(Intent.ACTION_SEND)
                                    intent.type = "text/plain"
                                    intent.putExtra(Intent.EXTRA_TEXT, mensaje)
                                    context.startActivity(
                                        Intent.createChooser(
                                            intent,
                                            "Compartir recordatorio"
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TealPastel,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(0.9f),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Compartir")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Recordar por WhatsApp")
                            }
                        }
                    }
                }
            }

            // Estado de carga
            uiState.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        val loadingComposition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(R.raw.movie_animation)
                        )

                        // Envolver en key para forzar recomposición limpia
                        key("loading_animation") {
                            LottieAnimation(
                                composition = loadingComposition,
                                iterations = LottieConstants.IterateForever,
                                restartOnPlay = true,
                                renderMode = RenderMode.SOFTWARE,
                                modifier = Modifier
                                    .size(250.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }

                        Text(
                            "Cargando contenido...",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
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
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Icono de palomitas simple
                        Icon(
                            painter = painterResource(id = R.drawable.ic_popcorn),
                            contentDescription = null,
                            tint = TealPastel,
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "¡Todo visto!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Has revisado todo el contenido disponible.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Matches detectados
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "${uiState.totalMatchesGrupo}",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPastel
                                )
                                Text(
                                    "Matches detectados",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.reiniciar() },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TealPastel,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Reiniciar",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Reiniciar",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }

            // Mostrar tarjeta
            uiState.contenidoActual != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 80.dp, end = 16.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Box para apilar las cards (siguiente card debajo)
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(top = 8.dp, bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Segunda card (siguiente película) - mostrar solo si hay siguiente contenido
                        uiState.contenidoSiguiente?.let { siguienteContenido ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.65f)
                                    .graphicsLayer {
                                        scaleX = 0.95f
                                        scaleY = 0.95f
                                        translationY = 10f
                                    },
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Poster de la siguiente película
                                    AsyncImage(
                                        model = siguienteContenido.posterUrl,
                                        contentDescription = siguienteContenido.titulo,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Overlay oscuro para indicar que es la siguiente
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f))
                                    )
                                }
                            }
                        }

                        // Tarjeta principal swipeable con flip
                        // Usar key para forzar recomposición cuando cambia el contenido
                        key(uiState.contenidoActual!!.idContenido) {
                            FlippableSwipeCard(
                                contenido = uiState.contenidoActual!!,
                                onSwipeLeft = { viewModel.onSwipeLeft() },
                                onSwipeRight = { viewModel.onSwipeRight() },
                                onLoadDetails = { contenido ->
                                    viewModel.loadContentDetails(contenido)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
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
                    viewModel.continuarDespuesDeMatch()
                    onVerMatches() // Navegar a la pantalla de matches
                }
            )
        }

        // Botón de volver (siempre visible)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 20.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.5f)
                    )
                    .background(
                        color = SurfaceLight.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
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
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Box para superponer título sobre la animación
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Animación de match DENTRO del card
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.match_animation))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.TopCenter)
                    )

                    // Título superpuesto sobre la animación
                    Text(
                        text = "¡Es un match!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TealPastel,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 20.dp) // Subir un poco para superponerlo
                    )
                }

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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contenido.titulo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            color = Color.White
                        )
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
                    Text(
                        text = "Descubrir más",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
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
