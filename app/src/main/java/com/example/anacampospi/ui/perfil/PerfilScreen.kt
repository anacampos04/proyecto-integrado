package com.example.anacampospi.ui.perfil

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anacampospi.modelo.PlataformasCatalogo
import com.example.anacampospi.viewModels.PerfilViewModel
import com.example.anacampospi.ui.componentes.*
import com.example.anacampospi.ui.theme.*

/**
 * Pantalla de perfil del usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onLogout: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarSelectorPlataformas by remember { mutableStateOf(false) }
    var mostrarEditarNombre by remember { mutableStateOf(false) }
    var mostrarEditarRegion by remember { mutableStateOf(false) }

    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
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
        Column(modifier = Modifier.fillMaxSize()) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mi perfil",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = {
                                viewModel.cerrarSesion()
                                onLogout()
                            }
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = CinemaRed
                            )
                        }
                    }
                }
            }

            // Contenido
            if (uiState.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TealPastel)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Avatar y nombre
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 100)) +
                                slideInVertically(initialOffsetY = { -50 })
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Avatar con icono
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = CircleShape,
                                        ambientColor = GlowTeal,
                                        spotColor = GlowTeal
                                    )
                                    .clip(CircleShape)
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
                                    text = (uiState.usuario?.nombre?.firstOrNull()?.uppercase() ?: "U"),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                            // Nombre con botón de editar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = uiState.usuario?.nombre ?: "Usuario",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                IconButton(
                                    onClick = { mostrarEditarNombre = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar nombre",
                                        tint = TealPastel,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Email
                            Text(
                                text = uiState.usuario?.correo ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.7f)
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

                    // Sección de plataformas
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                                slideInVertically(initialOffsetY = { 50 })
                    ) {
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Mis plataformas",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    ModernTextButton(
                                        onClick = { mostrarSelectorPlataformas = true },
                                        text = "Editar"
                                    )
                                }

                                // Mostrar plataformas seleccionadas
                                if (uiState.usuario?.plataformas.isNullOrEmpty()) {
                                    Text(
                                        text = "No has seleccionado plataformas aún",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                } else {
                                    val plataformas = PlataformasCatalogo.getPlataformas(
                                        uiState.usuario?.plataformas ?: emptyList()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        plataformas.take(5).forEach { plataforma ->
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .shadow(
                                                        elevation = 4.dp,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(SurfaceDark.copy(alpha = 0.6f))
                                                    .padding(8.dp)
                                            ) {
                                                Image(
                                                    painter = painterResource(id = plataforma.icono),
                                                    contentDescription = plataforma.nombre,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                        }
                                        if (plataformas.size > 5) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .shadow(
                                                        elevation = 4.dp,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(TealPastel),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "+${plataformas.size - 5}",
                                                    color = Color.Black,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Región
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 300)) +
                                slideInVertically(initialOffsetY = { 50 })
                    ) {
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        tint = TealPastel,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Región",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = uiState.usuario?.region ?: "ES",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                IconButton(onClick = { mostrarEditarRegion = true }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar región",
                                        tint = TealPastel
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Modal selector de plataformas
    if (mostrarSelectorPlataformas) {
        SelectorPlataformasModal(
            plataformasSeleccionadas = uiState.usuario?.plataformas ?: emptyList(),
            onDismiss = { mostrarSelectorPlataformas = false },
            onGuardar = { plataformas ->
                viewModel.actualizarPlataformas(plataformas)
                mostrarSelectorPlataformas = false
            }
        )
    }

    // Modal de edición de nombre
    if (mostrarEditarNombre) {
        EditarTextoModal(
            titulo = "Editar nombre",
            valorActual = uiState.usuario?.nombre ?: "",
            onDismiss = { mostrarEditarNombre = false },
            onGuardar = { nuevoNombre ->
                viewModel.actualizarNombre(nuevoNombre)
                mostrarEditarNombre = false
            }
        )
    }

    // Modal de edición de región
    if (mostrarEditarRegion) {
        EditarRegionModal(
            regionActual = uiState.usuario?.region ?: "ES",
            onDismiss = { mostrarEditarRegion = false },
            onGuardar = { nuevaRegion ->
                viewModel.actualizarRegion(nuevaRegion)
                mostrarEditarRegion = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorPlataformasModal(
    plataformasSeleccionadas: List<String>,
    onDismiss: () -> Unit,
    onGuardar: (List<String>) -> Unit
) {
    var seleccionadas by remember { mutableStateOf(plataformasSeleccionadas.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(24.dp),
        confirmButton = {
            Button(
                onClick = { onGuardar(seleccionadas.toList()) },
                enabled = seleccionadas.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPastel,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text("Cancelar")
            }
        },
        title = {
            Text(
                "Selecciona tus plataformas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(PlataformasCatalogo.TODAS) { plataforma ->
                    val isSelected = seleccionadas.contains(plataforma.id)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .shadow(
                                elevation = if (isSelected) 8.dp else 2.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = if (isSelected) GlowTeal else Color.Black.copy(alpha = 0.3f)
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
                                            SurfaceDark.copy(alpha = 0.8f),
                                            Color.Black.copy(alpha = 0.6f)
                                        )
                                    )
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) TealPastel else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                seleccionadas = if (isSelected) {
                                    seleccionadas - plataforma.id
                                } else {
                                    seleccionadas + plataforma.id
                                }
                            }
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
                            )
                        }
                    }
                }
            }
        }
    )
}

/**
 * Modal genérico para editar texto - Estilo moderno
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarTextoModal(
    titulo: String,
    valorActual: String,
    onDismiss: () -> Unit,
    onGuardar: (String) -> Unit,
    hint: String = ""
) {
    var texto by remember { mutableStateOf(valorActual) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(24.dp),
        confirmButton = {
            Button(
                onClick = { onGuardar(texto) },
                enabled = texto.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPastel,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text("Cancelar")
            }
        },
        title = {
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            ModernTextField(
                value = texto,
                onValueChange = { texto = it },
                label = titulo,
                singleLine = true
            )
        }
    )
}

/**
 * Modal para editar región con desplegable de opciones comunes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarRegionModal(
    regionActual: String,
    onDismiss: () -> Unit,
    onGuardar: (String) -> Unit
) {
    // Regiones más comunes con código y nombre
    val regiones = listOf(
        "ES" to "España",
        "US" to "Estados Unidos",
        "GB" to "Reino Unido",
        "FR" to "Francia",
        "DE" to "Alemania",
        "IT" to "Italia",
        "PT" to "Portugal",
        "MX" to "México",
        "AR" to "Argentina",
        "CO" to "Colombia",
        "CL" to "Chile",
        "PE" to "Perú",
        "BR" to "Brasil",
        "CA" to "Canadá",
        "AU" to "Australia",
        "NL" to "Países Bajos",
        "BE" to "Bélgica",
        "CH" to "Suiza",
        "AT" to "Austria",
        "SE" to "Suecia",
        "NO" to "Noruega",
        "DK" to "Dinamarca",
        "FI" to "Finlandia",
        "IE" to "Irlanda",
        "PL" to "Polonia",
        "CZ" to "República Checa",
        "RO" to "Rumania",
        "GR" to "Grecia",
        "TR" to "Turquía",
        "JP" to "Japón",
        "KR" to "Corea del Sur",
        "CN" to "China",
        "IN" to "India",
        "NZ" to "Nueva Zelanda"
    )

    var regionSeleccionada by remember { mutableStateOf(regionActual) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(24.dp),
        confirmButton = {
            Button(
                onClick = { onGuardar(regionSeleccionada) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPastel,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text("Cancelar")
            }
        },
        title = {
            Text(
                "Cambiar región",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Selecciona tu región para obtener información de plataformas correcta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = regiones.find { it.first == regionSeleccionada }?.let { "${it.first} - ${it.second}" } ?: regionSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Región", color = Color.White.copy(alpha = 0.7f)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark.copy(alpha = 0.6f),
                            unfocusedContainerColor = SurfaceDark.copy(alpha = 0.6f),
                            focusedBorderColor = TealPastel,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(SurfaceLight)
                            .heightIn(max = 400.dp)
                    ) {
                        regiones.forEach { (codigo, nombre) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "$codigo - $nombre",
                                        color = if (codigo == regionSeleccionada) TealPastel else Color.White
                                    )
                                },
                                onClick = {
                                    regionSeleccionada = codigo
                                    expanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.White
                                ),
                                modifier = Modifier.background(
                                    if (codigo == regionSeleccionada)
                                        TealPastel.copy(alpha = 0.2f)
                                    else
                                        Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    )
}
