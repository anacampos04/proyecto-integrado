package com.example.anacampospi.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anacampospi.BuildConfig
import com.example.anacampospi.data.tmdb.TmdbClient
import com.example.anacampospi.data.tmdb.TmdbRepository
import com.example.anacampospi.modelo.ContenidoLite
import com.example.anacampospi.modelo.enums.TipoContenido
import com.example.anacampospi.modelo.enums.ValorVoto
import com.example.anacampospi.repositorio.AuthRepository
import com.example.anacampospi.repositorio.VotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la sesión de swipe
 */
class SwipeViewModel : ViewModel() {

    private val tmdbRepository: TmdbRepository = TmdbClient.createRepository(BuildConfig.TMDB_API_KEY)
    private val votoRepository = VotoRepository()
    private val authRepository = AuthRepository()

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

    /**
     * Carga contenido inicial con filtros
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
                contentStack.addAll(contenido.filterVotados())
                currentPage = 1

                _uiState.value = _uiState.value.copy(
                    contenidoActual = contentStack.firstOrNull(),
                    contenidoRestante = contentStack.size - 1,
                    loading = false,
                    error = null
                )
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
     * Verifica si hay un match para el contenido dado
     */
    private fun verificarMatch(contenido: ContenidoLite) {
        viewModelScope.launch {
            val uid = authRepository.currentUid() ?: return@launch

            val result = votoRepository.verificarMatch(uid, contenido.idContenido)

            result.onSuccess { hayMatch ->
                if (hayMatch) {
                    android.util.Log.d("SwipeViewModel", "¡MATCH encontrado! ${contenido.titulo}")
                    _uiState.value = _uiState.value.copy(
                        hayMatch = true,
                        contenidoMatch = contenido
                    )
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
        cargarContenido()
        _uiState.value = SwipeUiState()
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
    val contenidoMatch: ContenidoLite? = null
)
