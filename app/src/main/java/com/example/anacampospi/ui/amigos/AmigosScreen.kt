package com.example.anacampospi.ui.amigos

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anacampospi.modelo.Usuario
import com.example.anacampospi.ui.componentes.SwipeToDismissItem
import com.example.anacampospi.ui.theme.*
import com.example.anacampospi.viewModels.AmigosViewModel

/**
 * Pantalla para gestionar amigos: mostrar código propio, buscar usuarios y ver amigos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmigosScreen(
    viewModel: AmigosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var codigoBusqueda by remember { mutableStateOf("PCT-") }
    var visible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        visible = true
    }

    // Mostrar mensajes de éxito/error de búsqueda
    LaunchedEffect(uiState.mensajeExito, uiState.errorBusqueda) {
        if (uiState.mensajeExito != null || uiState.errorBusqueda != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.limpiarMensajes()
        }
    }

    // Mostrar error de amigos como Snackbar
    LaunchedEffect(uiState.errorAmigos) {
        uiState.errorAmigos?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            kotlinx.coroutines.delay(2000)
            viewModel.limpiarErrorAmigos()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = CinemaRed.copy(alpha = 0.9f),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
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
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Mis amigos",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección: Tu código
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(500, 100)) + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500, 100)
                )
            ) {
                MiCodigoCard(
                    codigo = uiState.codigoPropio,
                    nombreUsuario = uiState.nombreUsuario
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Buscar por código
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(500, 200)) + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500, 200)
                )
            ) {
                BuscarUsuarioCard(
                    codigoBusqueda = codigoBusqueda,
                    onCodigoChange = { codigoBusqueda = it },
                    onBuscar = { viewModel.buscarPorCodigo(codigoBusqueda) },
                    buscando = uiState.buscando,
                    usuarioEncontrado = uiState.usuarioEncontrado,
                    yaSonAmigos = uiState.yaSonAmigos,
                    onAñadirAmigo = { viewModel.añadirAmigo() },
                    añadiendoAmigo = uiState.añadiendoAmigo,
                    onLimpiar = {
                        codigoBusqueda = "PCT-"
                        viewModel.limpiarBusqueda()
                    }
                )
            }

            // Mensajes
            uiState.mensajeExito?.let { mensaje ->
                Spacer(modifier = Modifier.height(8.dp))
                MensajeCard(mensaje, isError = false)
            }

            uiState.errorBusqueda?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                MensajeCard(error, isError = true)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Solicitudes recibidas (pendientes)
            if (uiState.solicitudesPendientes.isNotEmpty()) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(500, 250)) + slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(500, 250)
                    )
                ) {
                    SolicitudesPendientesSection(
                        solicitudes = uiState.solicitudesPendientes,
                        onAceptar = { viewModel.aceptarSolicitud(it) },
                        onRechazar = { viewModel.rechazarSolicitud(it) }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sección: Solicitudes enviadas
            if (uiState.solicitudesEnviadas.isNotEmpty()) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(500, 275)) + slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(500, 275)
                    )
                ) {
                    SolicitudesEnviadasSection(
                        solicitudes = uiState.solicitudesEnviadas,
                        onCancelar = { viewModel.cancelarSolicitud(it) }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sección: Lista de amigos
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(500, 300)) + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500, 300)
                )
            ) {
                ListaAmigosSection(
                    amigos = uiState.amigos,
                    cargando = uiState.cargandoAmigos,
                    onEliminar = { viewModel.eliminarAmigo(it) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun MiCodigoCard(codigo: String, nombreUsuario: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.QrCode2,
                contentDescription = null,
                tint = TealPastel,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tu código de invitación",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            if (nombreUsuario.isNotBlank()) {
                Text(
                    text = nombreUsuario,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Código destacado
            Box(
                modifier = Modifier
                    .border(2.dp, TealPastel, RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = codigo.ifBlank { "---" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = TealPastel,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Comparte este código con tus amigos",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BuscarUsuarioCard(
    codigoBusqueda: String,
    onCodigoChange: (String) -> Unit,
    onBuscar: () -> Unit,
    buscando: Boolean,
    usuarioEncontrado: Usuario?,
    yaSonAmigos: Boolean,
    onAñadirAmigo: () -> Unit,
    añadiendoAmigo: Boolean,
    onLimpiar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = TealPastel,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Añadir amigo",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de búsqueda con prefijo PCT-
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prefijo fijo
                Text(
                    text = "PCT-",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TealPastel,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 0.dp)
                )

                // Campo de entrada sin el prefijo
                OutlinedTextField(
                    value = codigoBusqueda.removePrefix("PCT-"),
                    onValueChange = { newValue ->
                        // Siempre añadir el prefijo PCT-
                        onCodigoChange("PCT-$newValue")
                    },
                    label = { Text("Código de invitación") },
                    placeholder = { Text("XXXX") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPastel,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedLabelColor = TealPastel,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    trailingIcon = {
                        if (codigoBusqueda.removePrefix("PCT-").isNotBlank()) {
                            IconButton(onClick = onLimpiar) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón buscar
            Button(
                onClick = onBuscar,
                modifier = Modifier.fillMaxWidth(),
                enabled = !buscando && codigoBusqueda.removePrefix("PCT-").isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPastel,
                    contentColor = Color.Black
                )
            ) {
                if (buscando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (buscando) "Buscando..." else "Buscar")
            }

            // Resultado de búsqueda
            usuarioEncontrado?.let { usuario ->
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))

                UsuarioEncontradoItem(
                    usuario = usuario,
                    yaSonAmigos = yaSonAmigos,
                    onAñadirAmigo = onAñadirAmigo,
                    añadiendoAmigo = añadiendoAmigo
                )
            }
        }
    }
}

@Composable
fun UsuarioEncontradoItem(
    usuario: Usuario,
    yaSonAmigos: Boolean,
    onAñadirAmigo: () -> Unit,
    añadiendoAmigo: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombre.ifBlank { "Usuario sin nombre" },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            if (yaSonAmigos) {
                Text(
                    text = "Ya sois amigos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TealPastel
                )
            } else {
                Button(
                    onClick = onAñadirAmigo,
                    enabled = !añadiendoAmigo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPastel,
                        contentColor = Color.Black
                    )
                ) {
                    if (añadiendoAmigo) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.Black
                        )
                    } else {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir")
                    }
                }
            }
        }
    }
}

@Composable
fun ListaAmigosSection(
    amigos: List<Usuario>,
    cargando: Boolean,
    onEliminar: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    tint = TealPastel,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Mis amigos",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                cargando -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TealPastel)
                    }
                }

                amigos.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PersonOff,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aún no tienes amigos",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Añade amigos usando su código",
                            color = Color.White.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        amigos.forEach { amigo ->
                            AmigoItem(
                                amigo = amigo,
                                onEliminar = { onEliminar(amigo.idUsuario) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AmigoItem(
    amigo: Usuario,
    onEliminar: () -> Unit
) {
    SwipeToDismissItem(
        onDelete = onEliminar,
        cornerRadius = 12.dp
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(TealPastel.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = TealPastel,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = amigo.nombre.ifBlank { "Usuario sin nombre" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = amigo.codigoInvitacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun MensajeCard(mensaje: String, isError: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
            } else {
                TealPastel.copy(alpha = 0.2f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else TealPastel
            )
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) MaterialTheme.colorScheme.error else Color.White
            )
        }
    }
}

/**
 * Sección de solicitudes de amistad pendientes (recibidas)
 */
@Composable
fun SolicitudesPendientesSection(
    solicitudes: List<Pair<com.example.anacampospi.modelo.SolicitudAmistad, Usuario>>,
    onAceptar: (String) -> Unit,
    onRechazar: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = TealPastel,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Solicitudes recibidas",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                solicitudes.forEach { (solicitud, usuario) ->
                    SolicitudPendienteItem(
                        usuario = usuario,
                        onAceptar = { onAceptar(solicitud.idSolicitud) },
                        onRechazar = { onRechazar(solicitud.idSolicitud) }
                    )
                }
            }
        }
    }
}

/**
 * Item individual de solicitud pendiente
 */
@Composable
fun SolicitudPendienteItem(
    usuario: Usuario,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Night.copy(alpha = 0.5f)
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
                        .clip(RoundedCornerShape(20.dp))
                        .background(TealPastel.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = TealPastel,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = usuario.nombre.ifBlank { "Usuario sin nombre" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = usuario.codigoInvitacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Botones
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rechazar
                IconButton(
                    onClick = onRechazar,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Rechazar",
                        tint = CinemaRed
                    )
                }

                // Aceptar
                IconButton(
                    onClick = onAceptar,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Aceptar",
                        tint = TealPastel
                    )
                }
            }
        }
    }
}

/**
 * Sección de solicitudes enviadas
 */
@Composable
fun SolicitudesEnviadasSection(
    solicitudes: List<Pair<com.example.anacampospi.modelo.SolicitudAmistad, Usuario>>,
    onCancelar: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Solicitudes enviadas",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                solicitudes.forEach { (solicitud, usuario) ->
                    SolicitudEnviadaItem(
                        usuario = usuario,
                        onCancelar = { onCancelar(solicitud.idSolicitud) }
                    )
                }
            }
        }
    }
}

/**
 * Item individual de solicitud enviada
 */
@Composable
fun SolicitudEnviadaItem(
    usuario: Usuario,
    onCancelar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Night.copy(alpha = 0.5f)
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
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = usuario.nombre.ifBlank { "Usuario sin nombre" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Pendiente de aceptación",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // Botón cancelar
            TextButton(onClick = onCancelar) {
                Text(
                    text = "Cancelar",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
