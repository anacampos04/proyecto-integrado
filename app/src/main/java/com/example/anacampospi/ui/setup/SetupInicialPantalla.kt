@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.anacampospi.ui.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.anacampospi.modelo.PlataformaStreaming
import com.example.anacampospi.modelo.PlataformasCatalogo
import com.example.anacampospi.viewModels.SetupViewModel

/**
 * Pantalla de configuración inicial que se muestra la primera vez
 * que el usuario entra a la app después de registrarse.
 */
@Composable
fun SetupInicialScreen(
    vm: SetupViewModel,
    onComplete: () -> Unit
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.completado) {
        if (state.completado) onComplete()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Configura tu perfil") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Título y descripción
            Text(
                text = "¡Bienvenido! 🎬",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Selecciona las plataformas de streaming que tienes para personalizar tus recomendaciones",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Grid de plataformas
            Text(
                text = "Mis plataformas:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(PlataformasCatalogo.TODAS) { plataforma ->
                    PlataformaCard(
                        plataforma = plataforma,
                        seleccionada = state.plataformasSeleccionadas.contains(plataforma.id),
                        onToggle = { vm.togglePlataforma(plataforma.id) }
                    )
                }
            }

            // Botón de continuar
            Button(
                onClick = { vm.guardarConfiguracion() },
                enabled = state.plataformasSeleccionadas.isNotEmpty() && !state.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Continuar")
                }
            }

            // Mensaje de error
            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PlataformaCard(
    plataforma: PlataformaStreaming,
    seleccionada: Boolean,
    onToggle: () -> Unit
) {
    val colors = if (seleccionada) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    val border = if (seleccionada) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    Card(
        onClick = onToggle,
        colors = colors,
        border = border,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = plataforma.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = if (seleccionada) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}