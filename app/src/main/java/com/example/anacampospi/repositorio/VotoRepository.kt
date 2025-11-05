package com.example.anacampospi.repositorio

import com.example.anacampospi.modelo.Voto
import com.example.anacampospi.modelo.enums.ValorVoto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repositorio para gestionar votos en Firestore
 */
class VotoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val votosCollection = db.collection("votos")

    /**
     * Guarda un voto en Firestore
     */
    suspend fun guardarVoto(
        idUsuario: String,
        idContenido: String,
        valorVoto: ValorVoto
    ): Result<Voto> {
        return try {
            val idVoto = UUID.randomUUID().toString()

            val voto = Voto(
                idVoto = idVoto,
                idUsuario = idUsuario,
                idContenido = idContenido,
                voto = valorVoto,
                creadoEn = null // ServerTimestamp se añadirá automáticamente
            )

            votosCollection.document(idVoto).set(voto).await()

            Result.success(voto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los votos de un usuario
     */
    suspend fun getVotosUsuario(idUsuario: String): Result<List<Voto>> {
        return try {
            val snapshot = votosCollection
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .await()

            val votos = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Voto::class.java)
            }

            Result.success(votos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifica si un usuario ya votó un contenido específico
     */
    suspend fun yaVoto(idUsuario: String, idContenido: String): Boolean {
        return try {
            val snapshot = votosCollection
                .whereEqualTo("idUsuario", idUsuario)
                .whereEqualTo("idContenido", idContenido)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene el voto de un usuario para un contenido específico
     */
    suspend fun getVoto(idUsuario: String, idContenido: String): Voto? {
        return try {
            val snapshot = votosCollection
                .whereEqualTo("idUsuario", idUsuario)
                .whereEqualTo("idContenido", idContenido)
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(Voto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene todos los votos positivos (ME_GUSTA) de un usuario
     */
    suspend fun getLikes(idUsuario: String): Result<List<Voto>> {
        return try {
            val snapshot = votosCollection
                .whereEqualTo("idUsuario", idUsuario)
                .whereEqualTo("voto", ValorVoto.ME_GUSTA)
                .get()
                .await()

            val votos = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Voto::class.java)
            }

            Result.success(votos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un voto
     */
    suspend fun eliminarVoto(idVoto: String): Result<Unit> {
        return try {
            votosCollection.document(idVoto).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifica si hay un match para un contenido específico en un grupo.
     * Un match ocurre cuando TODOS los miembros del grupo dieron like al mismo contenido.
     * @param idUsuario ID del usuario actual
     * @param idContenido ID del contenido votado
     * @param todosMiembrosGrupo Lista de IDs de TODOS los miembros del grupo (incluyendo al usuario actual)
     */
    suspend fun verificarMatch(
        idUsuario: String,
        idContenido: String,
        todosMiembrosGrupo: List<String>
    ): Result<Boolean> {
        return try {
            // Si no hay grupo, no puede haber match
            if (todosMiembrosGrupo.isEmpty()) {
                return Result.success(false)
            }

            // Buscar TODOS los votos de ME_GUSTA para este contenido
            val snapshot = votosCollection
                .whereEqualTo("idContenido", idContenido)
                .whereEqualTo("voto", ValorVoto.ME_GUSTA)
                .get()
                .await()

            // Obtener los IDs de usuarios que votaron ME_GUSTA
            val usuariosQueVotaronLike = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Voto::class.java)?.idUsuario
            }.toSet()

            // MATCH solo si TODOS los miembros del grupo votaron ME_GUSTA
            val hayMatch = todosMiembrosGrupo.all { miembroId ->
                usuariosQueVotaronLike.contains(miembroId)
            }

            Result.success(hayMatch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
