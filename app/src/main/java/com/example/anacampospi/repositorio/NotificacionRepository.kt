package com.example.anacampospi.repositorio

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar notificaciones push
 *
 * IMPORTANTE: Las notificaciones se envían guardando documentos en Firestore.
 * Necesitarás configurar Cloud Functions que escuchen esta colección y envíen
 * las notificaciones usando el Admin SDK de Firebase.
 *
 * Ejemplo de Cloud Function (Node.js):
 * ```
 * exports.enviarNotificacion = functions.firestore
 *   .document('notificaciones/{notifId}')
 *   .onCreate(async (snap, context) => {
 *     const data = snap.data();
 *     const tokens = data.tokens;
 *     const message = {
 *       notification: {
 *         title: data.titulo,
 *         body: data.mensaje
 *       },
 *       data: data.data || {},
 *       tokens: tokens
 *     };
 *     await admin.messaging().sendMulticast(message);
 *     await snap.ref.delete(); // Eliminar después de enviar
 *   });
 * ```
 */
class NotificacionRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val colNotificaciones = db.collection("notificaciones")
    private val colUsuarios = db.collection("usuarios")

    /**
     * Envía notificación de invitación a fiesta a una lista de usuarios
     */
    suspend fun enviarNotificacionInvitacionRonda(
        uidsDestino: List<String>,
        nombreGrupo: String,
        grupoId: String,
        nombreCreador: String
    ): Result<Unit> {
        return try {
            // Obtener tokens FCM de los usuarios destino
            val tokens = obtenerTokensDeUsuarios(uidsDestino)

            if (tokens.isEmpty()) {
                return Result.success(Unit) // No hay tokens, pero no es error
            }

            // Crear documento de notificación para Cloud Functions
            val notificacion = mapOf(
                "tipo" to "invitacion_ronda",
                "tokens" to tokens,
                "titulo" to "Nueva fiesta 🎉",
                "mensaje" to "$nombreCreador te ha invitado a '$nombreGrupo'. ¡Empieza la fiesta!",
                "data" to mapOf(
                    "tipo" to "invitacion_ronda",
                    "grupoId" to grupoId,
                    "nombreGrupo" to nombreGrupo
                ),
                "creadoEn" to FieldValue.serverTimestamp()
            )

            colNotificaciones.add(notificacion).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Envía notificación cuando una fiesta se activa (todos han configurado)
     * @param uidsDestino Lista de UIDs a notificar
     * @param nombreGrupo Nombre de la fiesta
     * @param grupoId ID de la fiesta
     * @param uidExcluir UID del usuario que NO debe recibir la notificación (el último en configurar)
     */
    suspend fun enviarNotificacionRondaActivada(
        uidsDestino: List<String>,
        nombreGrupo: String,
        grupoId: String,
        uidExcluir: String? = null
    ): Result<Unit> {
        return try {
            // Filtrar el UID a excluir si existe
            val uidsANotificar = if (uidExcluir != null) {
                uidsDestino.filter { it != uidExcluir }
            } else {
                uidsDestino
            }

            if (uidsANotificar.isEmpty()) {
                return Result.success(Unit) // No hay nadie a quien notificar
            }

            val tokens = obtenerTokensDeUsuarios(uidsANotificar)

            if (tokens.isEmpty()) {
                return Result.success(Unit)
            }

            val notificacion = mapOf(
                "tipo" to "ronda_activada",
                "tokens" to tokens,
                "titulo" to "¡Fiesta lista! 🎉",
                "mensaje" to "'$nombreGrupo' está lista. ¡Empieza a hacer swipe!",
                "data" to mapOf(
                    "tipo" to "ronda_activada",
                    "grupoId" to grupoId,
                    "nombreGrupo" to nombreGrupo
                ),
                "creadoEn" to FieldValue.serverTimestamp()
            )

            colNotificaciones.add(notificacion).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Envía notificación cuando hay un nuevo match
     */
    suspend fun enviarNotificacionNuevoMatch(
        uidsDestino: List<String>,
        nombreGrupo: String,
        grupoId: String,
        tituloContenido: String,
        idContenido: String
    ): Result<Unit> {
        return try {
            val tokens = obtenerTokensDeUsuarios(uidsDestino)

            if (tokens.isEmpty()) {
                return Result.success(Unit)
            }

            val notificacion = mapOf(
                "tipo" to "nuevo_match",
                "tokens" to tokens,
                "titulo" to "¡Match en $nombreGrupo! 🍿",
                "mensaje" to "Tu grupo ha hecho Match. ¡Entra para descubrirlo!",
                "data" to mapOf(
                    "tipo" to "nuevo_match",
                    "grupoId" to grupoId,
                    "nombreGrupo" to nombreGrupo,
                    "tituloContenido" to tituloContenido,
                    "idContenido" to idContenido
                ),
                "creadoEn" to FieldValue.serverTimestamp()
            )

            colNotificaciones.add(notificacion).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Envía notificación cuando alguien te envía una petición de amistad
     */
    suspend fun enviarNotificacionPeticionAmistad(
        uidDestino: String,
        nombreRemitente: String,
        solicitudId: String
    ): Result<Unit> {
        return try {
            android.util.Log.d("NotificacionRepo", "Obteniendo token para: $uidDestino")
            val tokens = obtenerTokensDeUsuarios(listOf(uidDestino))
            android.util.Log.d("NotificacionRepo", "Tokens encontrados: ${tokens.size}")

            if (tokens.isEmpty()) {
                android.util.Log.w("NotificacionRepo", "No hay tokens FCM, no se envía notificación")
                return Result.success(Unit)
            }

            val notificacion = mapOf(
                "tipo" to "peticion_amistad",
                "tokens" to tokens,
                "titulo" to "Nueva solicitud de amistad",
                "mensaje" to "$nombreRemitente quiere ser tu amigo",
                "data" to mapOf(
                    "tipo" to "peticion_amistad",
                    "solicitudId" to solicitudId
                ),
                "creadoEn" to FieldValue.serverTimestamp()
            )

            android.util.Log.d("NotificacionRepo", "Creando documento en /notificaciones con ${tokens.size} tokens")
            try {
                val docRef = colNotificaciones.add(notificacion).await()
                android.util.Log.d("NotificacionRepo", "Documento creado exitosamente con ID: ${docRef.id}")
            } catch (e: Exception) {
                android.util.Log.e("NotificacionRepo", "ERROR al crear documento en Firestore", e)
                return Result.failure(e)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los tokens FCM de una lista de usuarios
     */
    private suspend fun obtenerTokensDeUsuarios(uids: List<String>): List<String> {
        return try {
            android.util.Log.d("NotificacionRepo", "obtenerTokens - UIDs solicitados: ${uids.joinToString()}")
            val tokens = mutableListOf<String>()

            // Firestore tiene límite de 10 elementos en 'in' queries
            // Dividir en lotes de 10
            uids.chunked(10).forEach { chunk ->
                android.util.Log.d("NotificacionRepo", "obtenerTokens - Query whereIn idUsuario: ${chunk.joinToString()}")
                val snapshot = colUsuarios
                    .whereIn("idUsuario", chunk)
                    .get()
                    .await()

                android.util.Log.d("NotificacionRepo", "obtenerTokens - Documentos encontrados: ${snapshot.documents.size}")

                snapshot.documents.forEach { doc ->
                    android.util.Log.d("NotificacionRepo", "obtenerTokens - Doc ID: ${doc.id}, idUsuario: ${doc.getString("idUsuario")}")
                    val token = doc.getString("fcmToken")
                    android.util.Log.d("NotificacionRepo", "obtenerTokens - Token: ${if (token.isNullOrBlank()) "VACÍO" else "OK (${token.take(20)}...)"}")
                    if (!token.isNullOrBlank()) {
                        tokens.add(token)
                    }
                }
            }

            android.util.Log.d("NotificacionRepo", "obtenerTokens - Total tokens obtenidos: ${tokens.size}")
            tokens
        } catch (e: Exception) {
            android.util.Log.e("NotificacionRepo", "obtenerTokens - Error al obtener tokens", e)
            emptyList()
        }
    }
}
