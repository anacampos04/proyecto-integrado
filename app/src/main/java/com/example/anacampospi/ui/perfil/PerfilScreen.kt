package com.example.anacampospi.ui.perfil

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
// coil.compose.AsyncImage eliminado - no usamos fotos de perfil
import com.example.anacampospi.modelo.PlataformasCatalogo
import com.example.anacampospi.viewModels.PerfilViewModel

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi perfil",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.cerrarSesion()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Nombre (con botón de editar)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = uiState.usuario?.nombre ?: "Usuario",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { mostrarEditarNombre = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar nombre",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Email
                Text(
                    text = uiState.usuario?.correo ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider()

                // Sección de plataformas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mis plataformas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { mostrarSelectorPlataformas = true }) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar")
                            }
                        }

                        // Mostrar plataformas seleccionadas
                        if (uiState.usuario?.plataformas.isNullOrEmpty()) {
                            Text(
                                text = "No has seleccionado plataformas aún",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val plataformas = PlataformasCatalogo.getPlataformas(
                                uiState.usuario?.plataformas ?: emptyList()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                plataformas.take(5).forEach { plataforma ->
                                    Image(
                                        painter = painterResource(id = plataforma.icono),
                                        contentDescription = plataforma.nombre,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(4.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                if (plataformas.size > 5) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+${plataformas.size - 5}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Región
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Región",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.usuario?.region ?: "ES",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = { mostrarEditarRegion = true }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar región",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
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
        EditarTextoModal(
            titulo = "Editar región",
            valorActual = uiState.usuario?.region ?: "ES",
            onDismiss = { mostrarEditarRegion = false },
            onGuardar = { nuevaRegion ->
                viewModel.actualizarRegion(nuevaRegion)
                mostrarEditarRegion = false
            },
            hint = "Código de región (ej: ES, US, GB)"
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
        confirmButton = {
            TextButton(
                onClick = { onGuardar(seleccionadas.toList()) },
                enabled = seleccionadas.isNotEmpty()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = {
            Text(
                "Selecciona tus plataformas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
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
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
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
                                tint = MaterialTheme.colorScheme.primary,
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
 * Modal genérico para editar texto
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
        confirmButton = {
            TextButton(
                onClick = { onGuardar(texto) },
                enabled = texto.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = {
            Text(titulo, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text(titulo) },
                placeholder = if (hint.isNotEmpty()) { { Text(hint) } } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
