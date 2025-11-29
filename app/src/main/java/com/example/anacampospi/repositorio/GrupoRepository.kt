package com.example.anacampospi.repositorio

import com.example.anacampospi.modelo.ConfiguracionUsuario
import com.example.anacampospi.modelo.FiltrosGrupo
import com.example.anacampospi.modelo.Grupo
import com.example.anacampospi.modelo.enums.EstadoGrupo
import com.example.anacampospi.modelo.enums.TipoContenido
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar grupos/rondas de swipe.
 */
class GrupoRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val colGrupos = db.collection("grupos")
    private val notificacionRepo = NotificacionRepository(db)
    private val usuarioRepo = UsuarioRepository(db)

    /**
     * Crea un nuevo grupo/ronda con configuración del creador.
     * El grupo queda en estado CONFIGURANDO esperando a que los demás usuarios configuren.
     *
     * @param creadoPor UID del usuario que crea el grupo
     * @param miembros Lista de UIDs de los miembros invitados (SIN incluir al creador)
     * @param nombrePersonalizado Nombre opcional personalizado para el grupo
     * @param plataformas Plataformas del creador
     * @param tipos Tipos de contenido elegidos por el creador (puede ser uno o ambos)
     * @param generos Géneros del creador
     * @return ID del grupo creado
     */
    suspend fun crearGrupo(
        creadoPor: String,
        miembros: List<String>,
        nombrePersonalizado: String? = null,
        plataformas: List<String>,
        tipos: List<TipoContenido>,
        generos: List<Int>
    ): Result<String> {
        return try {
            // Validar que al menos hay un tipo seleccionado
            if (tipos.isEmpty()) {
                return Result.failure(Exception("Debe seleccionar al menos un tipo de contenido"))
            }

            // Asegurarse de que el creador está en la lista de miembros
            val miembrosFinales = if (creadoPor !in miembros) {
                miembros + creadoPor
            } else {
                miembros
            }

            // Usar nombre personalizado si existe, sino generar uno automático
            val nombre = if (!nombrePersonalizado.isNullOrBlank()) {
                nombrePersonalizado
            } else {
                generarNombreGrupo(miembrosFinales.size)
            }

            // Configuración del creador (ya configurado)
            val configuracionCreador = ConfiguracionUsuario(
                plataformas = plataformas,
                generos = generos,
                configurado = true
            )

            // Filtros iniciales con tipos elegidos por el creador
            val filtros = FiltrosGrupo(
                plataformas = emptyList(), // Se llenará cuando todos configuren
                generos = emptyList(), // Se llenará cuando todos configuren
                tipos = tipos
            )

            val grupo = Grupo(
                nombre = nombre,
                miembros = miembrosFinales,
                creadoPor = creadoPor,
                estado = EstadoGrupo.CONFIGURANDO.name,
                configuraciones = mapOf(creadoPor to configuracionCreador),
                filtros = filtros
            )

            val docRef = colGrupos.add(grupo).await()

            // Actualizar el documento con su propio ID
            docRef.update("idGrupo", docRef.id).await()

            // Enviar notificaciones de invitación inmediatamente a los miembros
            try {
                val miembrosInvitados = miembrosFinales.filter { it != creadoPor }
                if (miembrosInvitados.isNotEmpty()) {
                    // Obtener nombre del creador
                    val usuarioCreador = usuarioRepo.getUsuario(creadoPor).getOrNull()
                    val nombreCreador = usuarioCreador?.nombre?.ifBlank { "Un amigo" } ?: "Un amigo"

                    notificacionRepo.enviarNotificacionInvitacionRonda(
                        uidsDestino = miembrosInvitados,
                        nombreGrupo = nombre,
                        grupoId = docRef.id,
                        nombreCreador = nombreCreador
                    )
                }
            } catch (e: Exception) {
                // No fallar la creación del grupo si falla el envío de notificaciones
                android.util.Log.e("GrupoRepository", "Error al enviar notificaciones de invitación", e)
            }

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Un usuario configura su parte de la ronda (plataformas + géneros).
     * Si es el último en configurar, actualiza filtros finales y cambia estado a ACTIVA.
     */
    suspend fun configurarRonda(
        idGrupo: String,
        uid: String,
        plataformas: List<String>,
        generos: List<Int>
    ): Result<Unit> {
        return try {
            val grupo = obtenerGrupo(idGrupo).getOrNull()
                ?: return Result.failure(Exception("Grupo no encontrado"))

            // Verificar que el usuario es miembro
            if (uid !in grupo.miembros) {
                return Result.failure(Exception("No eres miembro de esta ronda"))
            }

            // Verificar que no ha configurado ya
            if (grupo.configuraciones[uid]?.configurado == true) {
                return Result.failure(Exception("Ya has configurado esta ronda"))
            }

            // Crear configuración del usuario
            val configuracion = ConfiguracionUsuario(
                plataformas = plataformas,
                generos = generos,
                configurado = true
            )

            // Actualizar configuración del usuario
            val nuevoMapaConfiguraciones = grupo.configuraciones.toMutableMap()
            nuevoMapaConfiguraciones[uid] = configuracion

            // Actualizar en Firestore
            colGrupos.document(idGrupo).update(
                mapOf(
                    "configuraciones.$uid" to configuracion
                )
            ).await()

            // Verificar si todos han configurado
            val todosConfigurados = grupo.miembros.all { miembroUid ->
                nuevoMapaConfiguraciones[miembroUid]?.configurado == true
            }

            if (todosConfigurados) {
                // Calcular unión de filtros y activar ronda
                // Pasar el UID del usuario que acaba de configurar para excluirlo de la notificación
                activarRonda(idGrupo, nuevoMapaConfiguraciones, uid)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Activa la ronda cuando todos han configurado.
     * Calcula la unión de plataformas y géneros.
     * @param uidUltimoEnConfigurar UID del último usuario en configurar (no se le enviará notificación)
     */
    private suspend fun activarRonda(
        idGrupo: String,
        configuraciones: Map<String, ConfiguracionUsuario>,
        uidUltimoEnConfigurar: String? = null
    ) {
        try {
            val grupo = obtenerGrupo(idGrupo).getOrNull() ?: return

            // Unión de todas las plataformas
            val plataformasUnidas = configuraciones.values
                .flatMap { it.plataformas }
                .distinct()

            // Unión de todos los géneros
            val generosUnidos = configuraciones.values
                .flatMap { it.generos }
                .distinct()

            // Actualizar filtros finales y estado
            val filtrosFinales = grupo.filtros.copy(
                plataformas = plataformasUnidas,
                generos = generosUnidos
            )

            colGrupos.document(idGrupo).update(
                mapOf(
                    "estado" to EstadoGrupo.ACTIVA.name,
                    "filtros" to filtrosFinales,
                    "filtrosActualizadosEn" to FieldValue.serverTimestamp()
                )
            ).await()

            // Enviar notificación de ronda activada a todos EXCEPTO al último en configurar
            try {
                notificacionRepo.enviarNotificacionRondaActivada(
                    uidsDestino = grupo.miembros,
                    nombreGrupo = grupo.nombre,
                    grupoId = idGrupo,
                    uidExcluir = uidUltimoEnConfigurar
                )
            } catch (e: Exception) {
                android.util.Log.e("GrupoRepository", "Error al enviar notificaciones de activación", e)
            }
        } catch (e: Exception) {
            // Log error pero no fallar
            e.printStackTrace()
        }
    }

    /**
     * Obtiene todos los grupos en los que el usuario es miembro.
     * Ordenados por fecha de creación (más recientes primero).
     */
    suspend fun obtenerGruposDelUsuario(uid: String): Result<List<Grupo>> {
        return try {
            val snapshot = colGrupos
                .whereArrayContains("miembros", uid)
                .orderBy("creadoEn", Query.Direction.DESCENDING)
                .get()
                .await()

            val grupos = snapshot.documents.mapNotNull { doc ->
                val grupo = doc.toObject(Grupo::class.java)
                grupo?.copy(idGrupo = doc.id)
            }

            Result.success(grupos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene rondas activas (todas han configurado y se puede hacer swipe).
     */
    suspend fun obtenerRondasActivas(uid: String): Result<List<Grupo>> {
        return try {
            val todosGrupos = obtenerGruposDelUsuario(uid).getOrNull() ?: emptyList()
            val activas = todosGrupos.filter { it.estado == EstadoGrupo.ACTIVA.name }
            Result.success(activas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene rondas pendientes (esperando configuración del usuario actual).
     */
    suspend fun obtenerRondasPendientes(uid: String): Result<List<Grupo>> {
        return try {
            val todosGrupos = obtenerGruposDelUsuario(uid).getOrNull() ?: emptyList()
            val pendientes = todosGrupos.filter { grupo ->
                grupo.estado == EstadoGrupo.CONFIGURANDO.name &&
                grupo.configuraciones[uid]?.configurado != true
            }
            Result.success(pendientes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene rondas en configuración donde el usuario ya configuró pero faltan otros.
     */
    suspend fun obtenerRondasEsperando(uid: String): Result<List<Grupo>> {
        return try {
            val todosGrupos = obtenerGruposDelUsuario(uid).getOrNull() ?: emptyList()
            val esperando = todosGrupos.filter { grupo ->
                grupo.estado == EstadoGrupo.CONFIGURANDO.name &&
                grupo.configuraciones[uid]?.configurado == true
            }
            Result.success(esperando)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene un grupo por su ID.
     */
    suspend fun obtenerGrupo(idGrupo: String): Result<Grupo> {
        return try {
            val snapshot = colGrupos.document(idGrupo).get().await()
            val grupo = snapshot.toObject(Grupo::class.java)

            if (grupo != null) {
                Result.success(grupo)
            } else {
                Result.failure(Exception("Grupo no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un grupo (cualquier miembro puede eliminarlo).
     * Borra también los matches (subcollection) y los votos asociados.
     */
    suspend fun eliminarGrupo(idGrupo: String, uid: String): Result<Unit> {
        return try {
            val grupo = obtenerGrupo(idGrupo).getOrNull()

            if (grupo == null) {
                return Result.failure(Exception("Grupo no encontrado"))
            }

            // Verificar que el usuario sea miembro del grupo
            if (!grupo.miembros.contains(uid)) {
                return Result.failure(Exception("No puedes eliminar un grupo del que no eres miembro"))
            }

            // 1. Eliminar todos los matches (subcollection)
            val matchesSnapshot = colGrupos.document(idGrupo)
                .collection("matches")
                .get()
                .await()

            matchesSnapshot.documents.forEach { matchDoc ->
                matchDoc.reference.delete().await()
            }

            // 2. Eliminar todos los votos asociados a este grupo
            val votosSnapshot = db.collection("votos")
                .whereEqualTo("grupoId", idGrupo)
                .get()
                .await()

            votosSnapshot.documents.forEach { votoDoc ->
                votoDoc.reference.delete().await()
            }

            // 3. Finalmente eliminar el documento del grupo
            colGrupos.document(idGrupo).delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Genera un nombre automático para el grupo basado en el número de miembros.
     */
    private fun generarNombreGrupo(numeroMiembros: Int): String {
        val nombres = listOf(
            "Spoiler: noche de pelis",
            "Mantita y pelis",
            "Solo un capítulo más",
            "Club de Cine",
            "Maratón de palomitas",
            "Viernes de Series",
            "Terapia de grupo",
            "Sesión Doble",
            "Desmadre pero no mucho",
            "Resacón en la casa"
        )
        return nombres.random()
    }

    /**
     * Guarda un match en la subcolección de matches del grupo.
     * Un match representa contenido que varios usuarios del grupo votaron positivamente.
     *
     * @param grupoId ID del grupo
     * @param match Objeto Match a guardar
     * @return Result indicando éxito o error
     */
    suspend fun guardarMatch(
        grupoId: String,
        match: com.example.anacampospi.modelo.Match
    ): Result<Unit> {
        return try {
            colGrupos
                .document(grupoId)
                .collection("matches")
                .document(match.idContenido)
                .set(match)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza un match existente añadiendo un nuevo usuario coincidente.
     * Si el match no existe, lo crea.
     *
     * @param grupoId ID del grupo
     * @param match Match a actualizar o crear
     * @return Result indicando éxito o error
     */
    suspend fun actualizarMatch(
        grupoId: String,
        match: com.example.anacampospi.modelo.Match
    ): Result<Unit> {
        return try {
            colGrupos
                .document(grupoId)
                .collection("matches")
                .document(match.idContenido)
                .set(match)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
