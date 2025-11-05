package com.example.anacampospi.modelo

import com.example.anacampospi.modelo.enums.EstadoGrupo
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Grupo(
    val idGrupo: String = "",
    val nombre: String = "",
    val miembros: List<String> = emptyList(), // ids de usuarios invitados
    val creadoPor: String = "", // id del usuario que ha creado el grupo

    // Estado de configuración
    val estado: String = EstadoGrupo.CONFIGURANDO.name, // CONFIGURANDO, ACTIVA, FINALIZADA
    val configuraciones: Map<String, ConfiguracionUsuario> = emptyMap(), // key = UID, value = configuración

    // Filtros finales (unión de todas las configuraciones)
    val filtros: FiltrosGrupo = FiltrosGrupo(),

    @ServerTimestamp val filtrosActualizadosEn: Timestamp? = null,
    @ServerTimestamp val creadoEn: Timestamp? = null
) {
    /**
     * Verifica si todos los usuarios han configurado la ronda
     */
    fun todosConfigurados(): Boolean {
        if (miembros.isEmpty()) return false
        return miembros.all { uid ->
            configuraciones[uid]?.configurado == true
        }
    }

    /**
     * Obtiene la lista de usuarios que aún no han configurado
     */
    fun usuariosPendientes(): List<String> {
        return miembros.filter { uid ->
            configuraciones[uid]?.configurado != true
        }
    }

    /**
     * Obtiene el estado como enum
     */
    fun getEstadoEnum(): EstadoGrupo {
        return try {
            EstadoGrupo.valueOf(estado)
        } catch (e: Exception) {
            EstadoGrupo.CONFIGURANDO
        }
    }
}
