package com.example.anacampospi.modelo

/**
 * Configuración de filtros que cada usuario aporta a una ronda.
 * Cuando todos los usuarios configuran, se hace la unión de plataformas y géneros.
 */
data class ConfiguracionUsuario(
    val plataformas: List<String> = emptyList(), // IDs de plataformas seleccionadas
    val generos: List<Int> = emptyList(), // IDs de géneros seleccionados
    val configurado: Boolean = false // true cuando el usuario ha terminado de configurar
)
