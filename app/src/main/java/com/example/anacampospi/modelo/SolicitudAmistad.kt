package com.example.anacampospi.modelo

import com.example.anacampospi.modelo.enums.EstadoSolicitud
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Modelo para las solicitudes de amistad.
 * Cuando un usuario añade a otro, se crea una solicitud PENDIENTE.
 * El destinatario puede aceptarla o rechazarla.
 */
data class SolicitudAmistad(
    val idSolicitud: String = "",
    val de: String = "", // UID del usuario que envía la solicitud
    val para: String = "", // UID del usuario que recibe la solicitud
    val estado: String = EstadoSolicitud.PENDIENTE.name, // Guardar como String para Firestore
    @ServerTimestamp val creadoEn: Timestamp? = null,
    @ServerTimestamp val actualizadoEn: Timestamp? = null
) {
    // Helper para obtener el estado como enum
    fun getEstadoEnum(): EstadoSolicitud {
        return try {
            EstadoSolicitud.valueOf(estado)
        } catch (e: Exception) {
            EstadoSolicitud.PENDIENTE
        }
    }
}
