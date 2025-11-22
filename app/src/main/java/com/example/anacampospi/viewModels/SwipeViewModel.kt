package com.example.anacampospi.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anacampospi.BuildConfig
import com.example.anacampospi.data.tmdb.TmdbClient
import com.example.anacampospi.data.tmdb.TmdbRepository
import com.example.anacampospi.modelo.ContenidoLite
import com.example.anacampospi.modelo.Grupo
import com.example.anacampospi.modelo.Usuario
import com.example.anacampospi.modelo.enums.TipoContenido
import com.example.anacampospi.modelo.enums.ValorVoto
import com.example.anacampospi.repositorio.AuthRepository
import com.example.anacampospi.repositorio.GrupoRepository
import com.example.anacampospi.repositorio.UsuarioRepository
import com.example.anacampospi.repositorio.VotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la sesión de swipe.
 * Soporta dos modos:
 * - Con grupoId: usa los filtros combinados del grupo (unión de configuraciones)
 * - Sin grupoId: usa los filtros pasados manualmente (compatibilidad)
 */
class SwipeViewModel : ViewModel() {

    private val tmdbRepository: TmdbRepository = TmdbClient.createRepository(BuildConfig.TMDB_API_KEY)
    private val votoRepository = VotoRepository()
    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val grupoRepository = GrupoRepository()

    private val _uiState = MutableStateFlow(SwipeUiState())
    val uiState: StateFlow<SwipeUiState> = _uiState.asStateFlow()

    // Pila de contenido para mostrar
    private val contentStack = mutableListOf<ContenidoLite>()
    private var currentPage = 1
    private var isLoadingMore = false

    // Filtros de configuración guardados para cargas posteriores
    private var filtroTipo: TipoContenido? = null
    private var filtroPlataformas: List<String>? = null
    private var filtroGeneros: List<Int>? = null
    private var filtroMinRating: Double? = null

    // ID del grupo (si se está usando modo grupo)
    private var currentGrupoId: String? = null

    // Miembros del grupo actual (para verificar matches)
    private var miembrosGrupo: List<String> = emptyList()

    /**
     * Inicializa el ViewModel con un grupo.
     * Carga el grupo y aplica sus filtros combinados.
     */
    fun inicializarConGrupo(grupoId: String) {
        currentGrupoId = grupoId

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            val result = grupoRepository.obtenerGrupo(grupoId)
            result.onSuccess { grupo ->
                // VERIFICAR QUE LA RONDA ESTÉ ACTIVA
                if (grupo.estado != "ACTIVA") {
                    // Cargar nombres de usuarios pendientes
                    val pendientesUids = grupo.usuariosPendientes()
                    val usuariosPendientes = mutableListOf<Usuario>()

                    pendientesUids.forEach { uid ->
                        val usuarioResult = UsuarioRepository().getUsuario(uid)
                        usuarioResult.onSuccess { usuario ->
                            usuariosPendientes.add(usuario)
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        esperandoOtrosUsuarios = true,
                        grupoActual = grupo,
                        usuariosPendientes = usuariosPendientes
                    )
                    return@onSuccess
                }

                // Guardar los miembros del grupo para verificar matches
                miembrosGrupo = grupo.miembros

                // Usar los filtros del grupo (ya tienen la unión de todas las configuraciones)
                val tipos = grupo.filtros.tipos
                val plataformas = grupo.filtros.plataformas
                val generos = grupo.filtros.generos

                // Determinar el tipo para la API:
                // - Si solo hay un tipo, usarlo
                // - Si hay ambos o ninguno, usar null (carga ambos)
                val tipoApi = when {
                    tipos.size == 1 -> tipos.first()
                    else -> null
                }

                cargarContenido(
                    tipo = tipoApi,
                    plataformas = plataformas.ifEmpty { null },
                    generos = generos.ifEmpty { null }
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Error al cargar el grupo: ${error.message}"
                )
            }
        }
    }

    /**
     * Carga contenido inicial con filtros (método manual para compatibilidad)
     */
    fun cargarContenido(
        tipo: TipoContenido? = null,
        plataformas: List<String>? = null,
        generos: List<Int>? = null,
        minRating: Double? = null
    ) {
        // Guardar filtros para cargas posteriores
        filtroTipo = tipo
        filtroPlataformas = plataformas
        filtroGeneros = generos
        filtroMinRating = minRating

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            val result = tmdbRepository.discoverContent(
                tipo = tipo,
                plataformas = plataformas,
                generos = generos,
                minRating = minRating,
                page = 1
            )

            result.onSuccess { contenido ->
                contentStack.clear()
                val contenidoFiltrado = contenido.filterVotados()
                contentStack.addAll(contenidoFiltrado)
                currentPage = 1

                // Si no hay contenido después de filtrar, marcar como sin contenido
                if (contentStack.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = null,
                        sinContenido = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        contenidoActual = contentStack.firstOrNull(),
                        contenidoRestante = contentStack.size - 1,
                        loading = false,
                        error = null,
                        sinContenido = false
                    )
                }
            }

            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = error.message ?: "Error al cargar contenido"
                )
            }
        }
    }

    /**
     * Carga más contenido cuando se está acabando
     * Usa los mismos filtros que la carga inicial
     */
    private fun cargarMasContenido() {
        if (isLoadingMore) return

        viewModelScope.launch {
            isLoadingMore = true
            currentPage++

            val result = tmdbRepository.discoverContent(
                tipo = filtroTipo,
                plataformas = filtroPlataformas,
                generos = filtroGeneros,
                minRating = filtroMinRating,
                page = currentPage
            )

            result.onSuccess { contenido ->
                contentStack.addAll(contenido.filterVotados())
                _uiState.value = _uiState.value.copy(
                    contenidoRestante = contentStack.size - 1
                )
            }

            isLoadingMore = false
        }
    }

    /**
     * Filtra contenido que ya fue votado por el usuario
     */
    private suspend fun List<ContenidoLite>.filterVotados(): List<ContenidoLite> {
        val uid = authRepository.currentUid() ?: return this

        return this.filter { contenido ->
            !votoRepository.yaVoto(uid, contenido.idContenido)
        }
    }

    /**
     * Maneja un swipe a la derecha (ME_GUSTA)
     */
    fun onSwipeRight() {
        val contenido = _uiState.value.contenidoActual ?: return
        guardarVoto(contenido, ValorVoto.ME_GUSTA)
        avanzarAlSiguiente()
    }

    /**
     * Maneja un swipe a la izquierda (NO_ME_GUSTA)
     */
    fun onSwipeLeft() {
        val contenido = _uiState.value.contenidoActual ?: return
        guardarVoto(contenido, ValorVoto.NO_ME_GUSTA)
        avanzarAlSiguiente()
    }

    /**
     * Guarda el voto en Firestore
     */
    private fun guardarVoto(contenido: ContenidoLite, valorVoto: ValorVoto) {
        viewModelScope.launch {
            val uid = authRepository.currentUid() ?: return@launch

            val result = votoRepository.guardarVoto(
                idUsuario = uid,
                idContenido = contenido.idContenido,
                valorVoto = valorVoto
            )

            result.onSuccess {
                // Log para debugging
                android.util.Log.d("SwipeViewModel", "Voto guardado: ${contenido.titulo} - $valorVoto")

                // Actualizar estadísticas
                if (valorVoto == ValorVoto.ME_GUSTA) {
                    _uiState.value = _uiState.value.copy(
                        totalLikes = _uiState.value.totalLikes + 1
                    )

                    // Verificar si hay match
                    verificarMatch(contenido)
                } else {
                    _uiState.value = _uiState.value.copy(
                        totalDislikes = _uiState.value.totalDislikes + 1
                    )
                }
            }

            result.onFailure { error ->
                // Log del error
                android.util.Log.e("SwipeViewModel", "Error guardando voto: ${error.message}", error)
            }
        }
    }

    /**
     * Verifica si hay un match para el contenido dado.
     * Un match ocurre cuando TODOS los miembros del grupo votaron ME_GUSTA.
     * Si hay match, lo guarda en Firestore.
     */
    private fun verificarMatch(contenido: ContenidoLite) {
        viewModelScope.launch {
            val uid = authRepository.currentUid() ?: return@launch

            // Si no hay grupo o no hay miembros, no puede haber match
            if (miembrosGrupo.isEmpty() || currentGrupoId == null) {
                android.util.Log.d("SwipeViewModel", "No hay grupo configurado, no se verifica match")
                return@launch
            }

            android.util.Log.d("SwipeViewModel", "Verificando match con miembros del grupo: $miembrosGrupo")

            val result = votoRepository.verificarMatch(uid, contenido.idContenido, miembrosGrupo)

            result.onSuccess { hayMatch ->
                if (hayMatch) {
                    android.util.Log.d("SwipeViewModel", "¡MATCH! TODOS los miembros votaron ME_GUSTA: ${contenido.titulo}")

                    // Guardar el match en Firestore
                    val match = com.example.anacampospi.modelo.Match(
                        idContenido = contenido.idContenido,
                        usuariosCoincidentes = miembrosGrupo,
                        primerCoincidenteEn = null, // ServerTimestamp
                        actualizadoEn = null,
                        titulo = contenido.titulo,
                        posterUrl = contenido.posterUrl,
                        tipo = contenido.tipo,
                        anioEstreno = contenido.anioEstreno,
                        proveedores = contenido.proveedores,
                        puntuacion = contenido.puntuacion
                    )

                    grupoRepository.guardarMatch(currentGrupoId!!, match).onSuccess {
                        android.util.Log.d("SwipeViewModel", "Match guardado en Firestore correctamente")
                    }.onFailure { error ->
                        android.util.Log.e("SwipeViewModel", "Error guardando match: ${error.message}", error)
                    }

                    // Actualizar UI para mostrar el diálogo
                    _uiState.value = _uiState.value.copy(
                        hayMatch = true,
                        contenidoMatch = contenido
                    )
                } else {
                    android.util.Log.d("SwipeViewModel", "No hay match aún. Esperando a que todos voten.")
                }
            }

            result.onFailure { error ->
                android.util.Log.e("SwipeViewModel", "Error verificando match: ${error.message}", error)
            }
        }
    }

    /**
     * Cierra el diálogo de match y continúa con el swipe
     */
    fun continuarDespuesDeMatch() {
        _uiState.value = _uiState.value.copy(
            hayMatch = false,
            contenidoMatch = null
        )
    }

    /**
     * Avanza al siguiente contenido
     */
    private fun avanzarAlSiguiente() {
        if (contentStack.isEmpty()) return

        contentStack.removeAt(0)

        // Si quedan pocas tarjetas, cargar más (aumentado a 10 para cargar antes)
        if (contentStack.size <= 10 && !isLoadingMore) {
            cargarMasContenido()
        }

        _uiState.value = _uiState.value.copy(
            contenidoActual = contentStack.firstOrNull(),
            contenidoRestante = contentStack.size - 1,
            sinContenido = false // Resetear el flag
        )

        // Si no hay más contenido Y no estamos cargando más
        if (contentStack.isEmpty() && !isLoadingMore) {
            _uiState.value = _uiState.value.copy(
                sinContenido = true
            )
        }
    }

    /**
     * Reinicia la sesión de swipe
     */
    fun reiniciar() {
        // Guardar los contadores actuales antes de resetear
        val likesActuales = _uiState.value.totalLikes
        val dislikesActuales = _uiState.value.totalDislikes

        // Resetear estado pero mantener contadores
        _uiState.value = SwipeUiState(
            totalLikes = likesActuales,
            totalDislikes = dislikesActuales
        )

        // Recargar contenido
        cargarContenido()
    }

    /**
     * Carga los detalles adicionales de un contenido bajo demanda
     * (providers y trailer cuando el usuario voltea la tarjeta)
     */
    fun loadContentDetails(contenido: ContenidoLite) {
        viewModelScope.launch {
            val result = tmdbRepository.enrichContentDetails(contenido)
            result.onSuccess { contenidoEnriquecido ->
                // Actualizar el contenido en el stack
                val index = contentStack.indexOfFirst { it.idContenido == contenidoEnriquecido.idContenido }
                if (index != -1) {
                    contentStack[index] = contenidoEnriquecido

                    // Si es el contenido actual, actualizar el UI state
                    if (_uiState.value.contenidoActual?.idContenido == contenidoEnriquecido.idContenido) {
                        _uiState.value = _uiState.value.copy(
                            contenidoActual = contenidoEnriquecido
                        )
                    }
                }
                android.util.Log.d("SwipeViewModel", "Detalles cargados: ${contenidoEnriquecido.titulo} - Providers: ${contenidoEnriquecido.proveedores.size}, Trailer: ${contenidoEnriquecido.trailer != null}")
            }
            result.onFailure { error ->
                android.util.Log.e("SwipeViewModel", "Error cargando detalles: ${error.message}", error)
            }
        }
    }
}

/**
 * Estado de UI para la sesión de swipe
 */
data class SwipeUiState(
    val contenidoActual: ContenidoLite? = null,
    val contenidoRestante: Int = 0,
    val totalLikes: Int = 0,
    val totalDislikes: Int = 0,
    val loading: Boolean = false,
    val error: String? = null,
    val sinContenido: Boolean = false,
    val hayMatch: Boolean = false,
    val contenidoMatch: ContenidoLite? = null,
    val esperandoOtrosUsuarios: Boolean = false,
    val grupoActual: Grupo? = null,
    val usuariosPendientes: List<Usuario> = emptyList()
)
