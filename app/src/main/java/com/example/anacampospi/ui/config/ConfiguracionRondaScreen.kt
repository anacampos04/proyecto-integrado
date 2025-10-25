package com.example.anacampospi.ui.config

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.anacampospi.data.tmdb.models.TmdbGenres
import com.example.anacampospi.modelo.PlataformasCatalogo
import com.example.anacampospi.modelo.enums.TipoContenido

/**
 * Pantalla de configuración de ronda de swipe
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionRondaScreen(
    onIniciarRonda: (plataformas: List<String>, tipo: TipoContenido?, generos: List<Int>) -> Unit,
    onBack: () -> Unit = {}
) {
    var plataformasSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    var tipoSeleccionado by remember { mutableStateOf<TipoContenido?>(null) }
    var generosSeleccionados by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Configurar ronda",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    onIniciarRonda(
                        plataformasSeleccionadas.toList(),
                        tipoSeleccionado,
                        generosSeleccionados.toList()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = plataformasSeleccionadas.isNotEmpty()
            ) {
                Text("Iniciar ronda", style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sección de plataformas
            Text(
                text = "Plataformas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(PlataformasCatalogo.TODAS) { plataforma ->
                    PlataformaItem(
                        plataforma = plataforma,
                        isSelected = plataformasSeleccionadas.contains(plataforma.id),
                        onClick = {
                            plataformasSeleccionadas = if (plataformasSeleccionadas.contains(plataforma.id)) {
                                plataformasSeleccionadas - plataforma.id
                            } else {
                                plataformasSeleccionadas + plataforma.id
                            }
                        }
                    )
                }
            }

            Divider()

            // Sección de tipo de contenido
            Text(
                text = "Tipo de contenido",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TipoChip(
                    label = "Ambos",
                    isSelected = tipoSeleccionado == null,
                    onClick = { tipoSeleccionado = null },
                    modifier = Modifier.weight(1f)
                )
                TipoChip(
                    label = "Películas",
                    isSelected = tipoSeleccionado == TipoContenido.PELICULA,
                    onClick = { tipoSeleccionado = TipoContenido.PELICULA },
                    modifier = Modifier.weight(1f)
                )
                TipoChip(
                    label = "Series",
                    isSelected = tipoSeleccionado == TipoContenido.SERIE,
                    onClick = { tipoSeleccionado = TipoContenido.SERIE },
                    modifier = Modifier.weight(1f)
                )
            }

            Divider()

            // Sección de géneros
            Text(
                text = "Géneros (opcional)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selecciona los géneros que te interesan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(TmdbGenres.getAllGenres()) { (id, nombre) ->
                    GeneroChip(
                        nombre = nombre,
                        isSelected = generosSeleccionados.contains(id),
                        onClick = {
                            generosSeleccionados = if (generosSeleccionados.contains(id)) {
                                generosSeleccionados - id
                            } else {
                                generosSeleccionados + id
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlataformaItem(
    plataforma: com.example.anacampospi.modelo.PlataformaStreaming,
    isSelected: Boolean,
    onClick: () -> Unit
) {
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            )
        }
    }
}

@Composable
private fun TipoChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier
    )
}

@Composable
private fun GeneroChip(
    nombre: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(nombre) },
        modifier = Modifier.fillMaxWidth()
    )
}
