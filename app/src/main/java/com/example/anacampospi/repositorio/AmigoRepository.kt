package com.example.anacampospi.repositorio

import com.example.anacampospi.modelo.Usuario
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar relaciones de amistad entre usuarios.
 * Utiliza el campo 'amigos' del modelo Usuario (cache de UIDs).
 */
class AmigoRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val col = db.collection("usuarios")

    /**
     * Añade una relación de amistad bidireccional entre dos usuarios.
     * Usa transacción para garantizar atomicidad.
     *
     * @param uidActual UID del usuario que añade al amigo
     * @param uidAmigo UID del usuario a añadir como amigo
     * @return Result.success si la operación fue exitosa
     */
    suspend fun añadirAmigo(uidActual: String, uidAmigo: String): Result<Unit> {
        return try {
            // Validaciones
            if (uidActual == uidAmigo) {
                return Result.failure(Exception("No puedes añadirte a ti mismo como amigo"))
            }

            // Verificar que el amigo existe
            val amigoDoc = col.document(uidAmigo).get().await()
            if (!amigoDoc.exists()) {
                return Result.failure(Exception("El usuario no existe"))
            }

            // Verificar si ya son amigos
            val actualDoc = col.document(uidActual).get().await()
            val usuario = actualDoc.toObject(Usuario::class.java)
            if (usuario?.amigos?.contains(uidAmigo) == true) {
                return Result.failure(Exception("Ya sois amigos"))
            }

            // Añadir relación bidireccional usando transacción
            db.runTransaction { transaction ->
                transaction.update(col.document(uidActual), "amigos", FieldValue.arrayUnion(uidAmigo))
                transaction.update(col.document(uidAmigo), "amigos", FieldValue.arrayUnion(uidActual))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una relación de amistad bidireccional.
     *
     * @param uidActual UID del usuario que elimina al amigo
     * @param uidAmigo UID del amigo a eliminar
     */
    suspend fun eliminarAmigo(uidActual: String, uidAmigo: String): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                transaction.update(col.document(uidActual), "amigos", FieldValue.arrayRemove(uidAmigo))
                transaction.update(col.document(uidAmigo), "amigos", FieldValue.arrayRemove(uidActual))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene la lista de amigos con sus datos completos.
     *
     * @param uid UID del usuario
     * @return Lista de objetos Usuario correspondientes a los amigos
     */
    suspend fun obtenerAmigos(uid: String): Result<List<Usuario>> {
        return try {
            val usuarioDoc = col.document(uid).get().await()
            val usuario = usuarioDoc.toObject(Usuario::class.java)
            val idsAmigos = usuario?.amigos.orEmpty()

            if (idsAmigos.isEmpty()) {
                return Result.success(emptyList())
            }

            // Firestore whereIn tiene límite de 10 elementos, así que procesamos en lotes
            val amigos = mutableListOf<Usuario>()
            idsAmigos.chunked(10).forEach { batch ->
                val snapshot = col
                    .whereIn("idUsuario", batch)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { it.toObject(Usuario::class.java) }
                    .let { amigos.addAll(it) }
            }

            Result.success(amigos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifica si dos usuarios son amigos.
     */
    suspend fun sonAmigos(uid1: String, uid2: String): Boolean {
        return try {
            val doc = col.document(uid1).get().await()
            val usuario = doc.toObject(Usuario::class.java)
            usuario?.amigos?.contains(uid2) == true
        } catch (e: Exception) {
            false
        }
    }
}
