package com.example.anacampospi.modelo.enums

/**
 * Estados posibles de un grupo/ronda
 */
enum class EstadoGrupo {
    CONFIGURANDO, // Esperando que todos los usuarios configuren
    ACTIVA,       // Todos configurados, se puede hacer swipe
    FINALIZADA    // Ronda terminada
}
