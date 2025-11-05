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
                "$nombrePersonalizado (${miembrosFinales.size})"
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
                activarRonda(idGrupo, nuevoMapaConfiguraciones)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Activa la ronda cuando todos han configurado.
     * Calcula la unión de plataformas y géneros.
     */
    private suspend fun activarRonda(
        idGrupo: String,
        configuraciones: Map<String, ConfiguracionUsuario>
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
     * Elimina un grupo (solo el creador puede eliminarlo).
     */
    suspend fun eliminarGrupo(idGrupo: String, uid: String): Result<Unit> {
        return try {
            val grupo = obtenerGrupo(idGrupo).getOrNull()

            if (grupo == null) {
                return Result.failure(Exception("Grupo no encontrado"))
            }

            if (grupo.creadoPor != uid) {
                return Result.failure(Exception("Solo el creador puede eliminar el grupo"))
            }

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
            "Noche de Pelis",
            "Cinéfilos Unidos",
            "Adictos a las Series",
            "Club de Cine",
            "Maratón de Películas",
            "Viernes de Series",
            "Palomitas y Diversión",
            "Sesión Doble",
            "Amantes del Cine",
            "Tribu del Sofá"
        )
        return "${nombres.random()} ($numeroMiembros)"
    }
}
