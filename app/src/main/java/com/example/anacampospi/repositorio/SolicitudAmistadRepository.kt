package com.example.anacampospi.repositorio

import com.example.anacampospi.modelo.SolicitudAmistad
import com.example.anacampospi.modelo.Usuario
import com.example.anacampospi.modelo.enums.EstadoSolicitud
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar solicitudes de amistad.
 */
class SolicitudAmistadRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val colSolicitudes = db.collection("solicitudesAmistad")
    private val colUsuarios = db.collection("usuarios")
    private val notificacionRepo = NotificacionRepository(db)

    /**
     * Envía una solicitud de amistad de un usuario a otro.
     * @param deUid UID del usuario que envía la solicitud
     * @param paraUid UID del usuario que recibe la solicitud
     */
    suspend fun enviarSolicitud(deUid: String, paraUid: String): Result<String> {
        return try {
            android.util.Log.d("SolicitudAmistadRepo", "Enviando solicitud de $deUid a $paraUid")

            // Validar que no se envíen a sí mismos
            if (deUid == paraUid) {
                return Result.failure(Exception("No puedes enviarte una solicitud a ti mismo"))
            }

            // Verificar que el destinatario existe
            val destinatario = colUsuarios.document(paraUid).get().await()
            if (!destinatario.exists()) {
                android.util.Log.e("SolicitudAmistadRepo", "El usuario $paraUid no existe")
                return Result.failure(Exception("El usuario no existe"))
            }

            android.util.Log.d("SolicitudAmistadRepo", "Destinatario verificado")

            // Obtener datos del remitente para verificaciones
            val remitente = colUsuarios.document(deUid).get().await()
            val usuario = remitente.toObject(Usuario::class.java)

            // Verificar si ya son amigos
            if (usuario?.amigos?.contains(paraUid) == true) {
                return Result.failure(Exception("Ya sois amigos"))
            }

            // Verificar si ya existe una solicitud pendiente
            val solicitudExistente = colSolicitudes
                .whereEqualTo("de", deUid)
                .whereEqualTo("para", paraUid)
                .whereEqualTo("estado", EstadoSolicitud.PENDIENTE.name)
                .limit(1)
                .get()
                .await()

            if (!solicitudExistente.isEmpty) {
                return Result.failure(Exception("Ya has enviado una solicitud a este usuario"))
            }

            // Verificar si existe una solicitud inversa pendiente
            val solicitudInversa = colSolicitudes
                .whereEqualTo("de", paraUid)
                .whereEqualTo("para", deUid)
                .whereEqualTo("estado", EstadoSolicitud.PENDIENTE.name)
                .limit(1)
                .get()
                .await()

            if (!solicitudInversa.isEmpty) {
                // Si el otro usuario ya te envió una solicitud, aceptarla automáticamente
                val solicitudId = solicitudInversa.documents.first().id
                val resultado = aceptarSolicitud(deUid, solicitudId)

                // Si la aceptación falló, propagar el error
                if (resultado.isFailure) {
                    return Result.failure(resultado.exceptionOrNull()
                        ?: Exception("Error al aceptar solicitud automáticamente"))
                }

                return Result.success("Solicitud aceptada automáticamente")
            }

            // Crear nueva solicitud
            // Nota: No guardamos idSolicitud en el documento porque usamos doc.id directamente
            android.util.Log.d("SolicitudAmistadRepo", "Creando solicitud en Firestore...")
            val docRef = colSolicitudes.add(
                hashMapOf(
                    "de" to deUid,
                    "para" to paraUid,
                    "estado" to EstadoSolicitud.PENDIENTE.name,
                    "creadoEn" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            ).await()

            android.util.Log.d("SolicitudAmistadRepo", "Solicitud creada con ID: ${docRef.id}")

            // Enviar notificación al destinatario
            try {
                val usuarioRemitente = remitente.toObject(Usuario::class.java)
                val nombreRemitente = usuarioRemitente?.nombre?.ifBlank { "Alguien" } ?: "Alguien"

                android.util.Log.d("SolicitudAmistadRepo", "Enviando notificación a $paraUid")
                notificacionRepo.enviarNotificacionPeticionAmistad(
                    uidDestino = paraUid,
                    nombreRemitente = nombreRemitente,
                    solicitudId = docRef.id
                )
                android.util.Log.d("SolicitudAmistadRepo", "Notificación enviada correctamente")
            } catch (e: Exception) {
                // No fallar el envío de solicitud si falla la notificación
                android.util.Log.e("SolicitudAmistadRepo", "Error al enviar notificación", e)
            }

            android.util.Log.d("SolicitudAmistadRepo", "Solicitud completada exitosamente")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Acepta una solicitud de amistad.
     * Añade ambos usuarios a sus respectivas listas de amigos.
     */
    suspend fun aceptarSolicitud(uidAceptador: String, solicitudId: String): Result<Unit> {
        return try {
            val solicitudDoc = colSolicitudes.document(solicitudId).get().await()
            val solicitud = solicitudDoc.toObject(SolicitudAmistad::class.java)
                ?: return Result.failure(Exception("Solicitud no encontrada"))

            // Verificar que el usuario que acepta es el destinatario
            if (solicitud.para != uidAceptador) {
                return Result.failure(Exception("No tienes permiso para aceptar esta solicitud"))
            }

            // Verificar que está pendiente
            if (solicitud.estado != EstadoSolicitud.PENDIENTE.name) {
                return Result.failure(Exception("Esta solicitud ya fue procesada"))
            }

            // Actualizar la solicitud y añadir a amigos en una transacción
            db.runTransaction { transaction ->
                // Actualizar estado de solicitud
                transaction.update(
                    colSolicitudes.document(solicitudId),
                    mapOf(
                        "estado" to EstadoSolicitud.ACEPTADA.name,
                        "actualizadoEn" to FieldValue.serverTimestamp()
                    )
                )

                // Añadir a amigos de forma bidireccional
                transaction.update(
                    colUsuarios.document(solicitud.de),
                    "amigos",
                    FieldValue.arrayUnion(solicitud.para)
                )
                transaction.update(
                    colUsuarios.document(solicitud.para),
                    "amigos",
                    FieldValue.arrayUnion(solicitud.de)
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Rechaza una solicitud de amistad.
     */
    suspend fun rechazarSolicitud(uidRechazador: String, solicitudId: String): Result<Unit> {
        return try {
            val solicitudDoc = colSolicitudes.document(solicitudId).get().await()
            val solicitud = solicitudDoc.toObject(SolicitudAmistad::class.java)
                ?: return Result.failure(Exception("Solicitud no encontrada"))

            // Verificar que el usuario que rechaza es el destinatario
            if (solicitud.para != uidRechazador) {
                return Result.failure(Exception("No tienes permiso para rechazar esta solicitud"))
            }

            // Actualizar estado
            colSolicitudes.document(solicitudId).update(
                mapOf(
                    "estado" to EstadoSolicitud.RECHAZADA.name,
                    "actualizadoEn" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Escucha las solicitudes pendientes en tiempo real.
     * Devuelve un ListenerRegistration que debe ser cancelado cuando ya no se necesite.
     */
    fun escucharSolicitudesPendientes(
        uid: String,
        onUpdate: (List<Pair<SolicitudAmistad, Usuario>>) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        android.util.Log.d("SolicitudAmistadRepo", "Iniciando listener para: $uid")

        return colSolicitudes
            .whereEqualTo("para", uid)
            .whereEqualTo("estado", EstadoSolicitud.PENDIENTE.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("SolicitudAmistadRepo", "Error en listener", error)
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    android.util.Log.w("SolicitudAmistadRepo", "Snapshot es null")
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                android.util.Log.d("SolicitudAmistadRepo", "Listener: ${snapshot.documents.size} documentos")

                // Procesar solicitudes en un scope de coroutines
                CoroutineScope(Dispatchers.IO).launch {
                    val solicitudes = snapshot.documents.mapNotNull { doc ->
                        android.util.Log.d("SolicitudAmistadRepo", "Listener - Doc: ${doc.id}")
                        val solicitud = doc.toObject(SolicitudAmistad::class.java)
                        solicitud?.copy(idSolicitud = doc.id)
                    }

                    // Obtener datos de usuarios
                    val solicitudesConUsuarios = solicitudes.mapNotNull { solicitud ->
                        try {
                            val usuarioDoc = colUsuarios.document(solicitud.de).get().await()
                            val usuario = usuarioDoc.toObject(Usuario::class.java)
                            if (usuario != null) {
                                Pair(solicitud, usuario)
                            } else null
                        } catch (e: Exception) {
                            android.util.Log.e("SolicitudAmistadRepo", "Error obteniendo usuario", e)
                            null
                        }
                    }

                    withContext(Dispatchers.Main) {
                        android.util.Log.d("SolicitudAmistadRepo", "Listener - Notificando ${solicitudesConUsuarios.size} solicitudes")
                        onUpdate(solicitudesConUsuarios)
                    }
                }
            }
    }

    /**
     * Obtiene las solicitudes pendientes que ha recibido un usuario.
     */
    suspend fun obtenerSolicitudesPendientes(uid: String): Result<List<Pair<SolicitudAmistad, Usuario>>> {
        return try {
            android.util.Log.d("SolicitudAmistadRepo", "Buscando solicitudes para: $uid")

            val snapshot = colSolicitudes
                .whereEqualTo("para", uid)
                .whereEqualTo("estado", EstadoSolicitud.PENDIENTE.name)
                .get()
                .await()

            android.util.Log.d("SolicitudAmistadRepo", "Documentos encontrados: ${snapshot.documents.size}")

            val solicitudes = snapshot.documents.mapNotNull { doc ->
                android.util.Log.d("SolicitudAmistadRepo", "Documento: ${doc.id}, de: ${doc.getString("de")}, para: ${doc.getString("para")}")
                val solicitud = doc.toObject(SolicitudAmistad::class.java)
                // Asegurar que el idSolicitud esté presente usando el ID del documento
                solicitud?.copy(idSolicitud = doc.id)
            }

            // Obtener datos de los usuarios que enviaron las solicitudes
            val solicitudesConUsuarios = solicitudes.mapNotNull { solicitud ->
                val usuarioDoc = colUsuarios.document(solicitud.de).get().await()
                val usuario = usuarioDoc.toObject(Usuario::class.java)
                if (usuario != null) {
                    Pair(solicitud, usuario)
                } else {
                    null
                }
            }

            android.util.Log.d("SolicitudAmistadRepo", "Solicitudes con usuario: ${solicitudesConUsuarios.size}")
            Result.success(solicitudesConUsuarios)
        } catch (e: Exception) {
            android.util.Log.e("SolicitudAmistadRepo", "Error al obtener solicitudes", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene las solicitudes enviadas por un usuario que están pendientes.
     */
    suspend fun obtenerSolicitudesEnviadas(uid: String): Result<List<Pair<SolicitudAmistad, Usuario>>> {
        return try {
            val solicitudes = colSolicitudes
                .whereEqualTo("de", uid)
                .whereEqualTo("estado", EstadoSolicitud.PENDIENTE.name)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    val solicitud = doc.toObject(SolicitudAmistad::class.java)
                    // Asegurar que el idSolicitud esté presente usando el ID del documento
                    solicitud?.copy(idSolicitud = doc.id)
                }

            // Obtener datos de los usuarios a los que se enviaron las solicitudes
            val solicitudesConUsuarios = solicitudes.mapNotNull { solicitud ->
                val usuarioDoc = colUsuarios.document(solicitud.para).get().await()
                val usuario = usuarioDoc.toObject(Usuario::class.java)
                if (usuario != null) {
                    Pair(solicitud, usuario)
                } else {
                    null
                }
            }

            Result.success(solicitudesConUsuarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancela una solicitud enviada (eliminarla).
     */
    suspend fun cancelarSolicitud(uidRemitente: String, solicitudId: String): Result<Unit> {
        return try {
            val solicitudDoc = colSolicitudes.document(solicitudId).get().await()
            val solicitud = solicitudDoc.toObject(SolicitudAmistad::class.java)
                ?: return Result.failure(Exception("Solicitud no encontrada"))

            // Verificar que el usuario que cancela es el remitente
            if (solicitud.de != uidRemitente) {
                return Result.failure(Exception("No tienes permiso para cancelar esta solicitud"))
            }

            // Eliminar la solicitud
            colSolicitudes.document(solicitudId).delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
