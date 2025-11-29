package com.example.anacampospi.ui.tutorial

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Overlay del tutorial que se muestra sobre la UI principal
 */
@Composable
fun TutorialOverlay(
    viewModel: TutorialViewModel,
    uiState: TutorialUiState
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = uiState.tutorialActivo && uiState.pasoActual != null,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            uiState.pasoActual?.let { paso ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Botón cerrar en la esquina
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { viewModel.saltarTutorial(context) }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Saltar tutorial",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Icon(
                            imageVector = paso.icono, // Usa la propiedad actualizada del data class
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp) // Tamaño grande para el icono principal
                                .padding(bottom = 16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        // Título
                        Text(
                            text = paso.titulo,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Descripción
                        Text(
                            text = paso.descripcion,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Indicador de progreso
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(uiState.totalPasos) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == uiState.pasoIndex) 10.dp else 8.dp)
                                        .background(
                                            color = if (index == uiState.pasoIndex)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(50)
                                        )
                                )
                                if (index < uiState.totalPasos - 1) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botones de navegación
                        if (viewModel.esPrimerPaso()) {
                            // Primer paso: solo botón centrado
                            Button(
                                onClick = {
                                    viewModel.siguientePaso()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(paso.accionBoton)
                            }
                        } else {
                            // Otros pasos: botón anterior + siguiente
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Botón anterior
                                OutlinedButton(
                                    onClick = { viewModel.pasoAnterior() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Volver")
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Botón siguiente/finalizar
                                Button(
                                    onClick = {
                                        if (viewModel.esUltimoPaso()) {
                                            viewModel.completarTutorial(context)
                                        } else {
                                            viewModel.siguientePaso()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(paso.accionBoton)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
