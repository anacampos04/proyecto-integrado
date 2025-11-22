package com.example.anacampospi.repositorio

import com.example.anacampospi.modelo.Match
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar matches (contenido que coincide entre usuarios).
 * Los matches se almacenan en: /grupos/{groupId}/matches/{contentId}
 */
class MatchesRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Obtiene todos los matches de un grupo específico, ordenados por fecha.
     *
     * @param grupoId ID del grupo
     * @return Lista de matches con el ID del grupo incluido
     */
    suspend fun obtenerMatchesDeGrupo(grupoId: String): Result<List<MatchConGrupo>> {
        return try {
            val snapshot = db.collection("grupos")
                .document(grupoId)
                .collection("matches")
                .orderBy("primerCoincidenteEn", Query.Direction.DESCENDING)
                .get()
                .await()

            val matches = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Match::class.java)?.let { match ->
                    MatchConGrupo(
                        match = match,
                        grupoId = grupoId,
                        grupoNombre = "" // Se llenará después
                    )
                }
            }

            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los matches de todos los grupos donde el usuario es miembro.
     *
     * @param userId UID del usuario
     * @return Lista de matches con información del grupo incluida
     */
    suspend fun obtenerTodosLosMatches(userId: String): Result<List<MatchConGrupo>> {
        return try {
            // 1. Obtener todos los grupos donde el usuario es miembro
            val gruposSnapshot = db.collection("grupos")
                .whereArrayContains("miembros", userId)
                .get()
                .await()

            val todosLosMatches = mutableListOf<MatchConGrupo>()

            // 2. Para cada grupo, obtener sus matches
            for (grupoDoc in gruposSnapshot.documents) {
                val grupoId = grupoDoc.id
                val grupoNombre = grupoDoc.getString("nombre") ?: "Grupo sin nombre"

                val matchesSnapshot = grupoDoc.reference
                    .collection("matches")
                    .get()
                    .await()

                val matchesDelGrupo = matchesSnapshot.documents.mapNotNull { matchDoc ->
                    matchDoc.toObject(Match::class.java)?.let { match ->
                        MatchConGrupo(
                            match = match,
                            grupoId = grupoId,
                            grupoNombre = grupoNombre
                        )
                    }
                }

                todosLosMatches.addAll(matchesDelGrupo)
            }

            // 3. Ordenar por fecha (más recientes primero)
            todosLosMatches.sortByDescending { it.match.primerCoincidenteEn }

            Result.success(todosLosMatches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene información del grupo (nombre, miembros) para un match.
     *
     * @param grupoId ID del grupo
     * @return Par de (nombre del grupo, lista de miembros UIDs)
     */
    suspend fun obtenerInfoGrupo(grupoId: String): Result<Pair<String, List<String>>> {
        return try {
            val doc = db.collection("grupos")
                .document(grupoId)
                .get()
                .await()

            val nombre = doc.getString("nombre") ?: "Grupo sin nombre"
            val miembros = doc.get("miembros") as? List<String> ?: emptyList()

            Result.success(Pair(nombre, miembros))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Clase auxiliar que combina un Match con información del grupo al que pertenece.
 */
data class MatchConGrupo(
    val match: Match,
    val grupoId: String,
    val grupoNombre: String
) {
    /**
     * Verifica si todos los miembros del grupo coincidieron en este match.
     * Requiere pasar la cantidad total de miembros del grupo.
     */
    fun esUnanimidad(totalMiembros: Int): Boolean {
        return match.usuariosCoincidentes.size == totalMiembros
    }
}
