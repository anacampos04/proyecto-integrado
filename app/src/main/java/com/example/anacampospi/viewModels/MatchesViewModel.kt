package com.example.anacampospi.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anacampospi.modelo.Match
import com.example.anacampospi.modelo.Usuario
import com.example.anacampospi.modelo.enums.TipoContenido
import com.example.anacampospi.repositorio.AuthRepository
import com.example.anacampospi.repositorio.MatchConGrupo
import com.example.anacampospi.repositorio.MatchesRepository
import com.example.anacampospi.repositorio.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la pantalla de matches.
 * Soporta dos modos:
 * - Modo general (grupoId = null): muestra matches de todos los grupos del usuario
 * - Modo específico (grupoId != null): muestra matches solo de ese grupo
 */
class MatchesViewModel : ViewModel() {

    private val matchesRepository = MatchesRepository()
    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    // Almacena todos los matches sin filtrar (para aplicar filtros locales)
    private var todosLosMatches: List<MatchConGrupoConUsuarios> = emptyList()

    // Grupos únicos disponibles (para los chips de filtro)
    private var gruposDisponibles: List<GrupoInfo> = emptyList()

    /**
     * Carga los matches según el modo.
     *
     * @param grupoId Si no es null, carga solo matches de ese grupo (modo específico)
     * @param grupoIdInicial Si no es null, pre-selecciona este grupo en el filtro (modo general)
     */
    fun cargarMatches(grupoId: String? = null, grupoIdInicial: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            val userId = authRepository.currentUser()?.uid
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Usuario no autenticado"
                )
                return@launch
            }

            val result = if (grupoId != null) {
                // Modo específico: solo un grupo
                matchesRepository.obtenerMatchesDeGrupo(grupoId)
            } else {
                // Modo general: todos los grupos
                matchesRepository.obtenerTodosLosMatches(userId!!) // Seguro después de la verificación
            }

            result.onSuccess { matchesList ->
                // Cargar información de usuarios para cada match
                val matchesConUsuarios = cargarInfoUsuarios(matchesList)

                todosLosMatches = matchesConUsuarios

                // Extraer grupos únicos para los filtros
                gruposDisponibles = matchesConUsuarios
                    .map { GrupoInfo(it.grupoId, it.grupoNombre) }
                    .distinctBy { it.id }

                // Pre-seleccionar grupo si se especificó
                val grupoSeleccionadoInicial = if (grupoIdInicial != null && gruposDisponibles.any { it.id == grupoIdInicial }) {
                    grupoIdInicial
                } else {
                    null
                }

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    modoGrupoEspecifico = grupoId != null,
                    gruposDisponibles = gruposDisponibles,
                    grupoSeleccionado = grupoSeleccionadoInicial
                )

                // Aplicar filtros (con el grupo pre-seleccionado si existe)
                aplicarFiltros()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Error al cargar matches: ${e.message}"
                )
            }
        }
    }

    /**
     * Carga la información de los usuarios que coincidieron en cada match.
     */
    private suspend fun cargarInfoUsuarios(
        matchesList: List<MatchConGrupo>
    ): List<MatchConGrupoConUsuarios> {
        return matchesList.map { matchConGrupo ->
            val usuariosInfo = mutableListOf<Usuario>()

            for (uid in matchConGrupo.match.usuariosCoincidentes) {
                val result = usuarioRepository.getUsuario(uid)
                result.onSuccess { usuario ->
                    usuariosInfo.add(usuario)
                }
            }

            MatchConGrupoConUsuarios(
                match = matchConGrupo.match,
                grupoId = matchConGrupo.grupoId,
                grupoNombre = matchConGrupo.grupoNombre,
                usuariosInfo = usuariosInfo
            )
        }
    }

    /**
     * Aplica los filtros actuales a la lista de matches.
     */
    private fun aplicarFiltros() {
        val state = _uiState.value

        var matchesFiltrados = todosLosMatches

        // Filtro por grupo (solo en modo general)
        if (!state.modoGrupoEspecifico && state.grupoSeleccionado != null) {
            matchesFiltrados = matchesFiltrados.filter { it.grupoId == state.grupoSeleccionado }
        }

        // Filtro por tipo de contenido
        when (state.filtroTipo) {
            FiltroTipo.PELICULAS -> matchesFiltrados = matchesFiltrados.filter {
                it.match.tipo == TipoContenido.PELICULA
            }
            FiltroTipo.SERIES -> matchesFiltrados = matchesFiltrados.filter {
                it.match.tipo == TipoContenido.SERIE
            }
            FiltroTipo.TODOS -> {} // No filtrar
        }

        _uiState.value = _uiState.value.copy(
            matches = matchesFiltrados,
            matchesVacios = matchesFiltrados.isEmpty()
        )
    }

    /**
     * Cambia el grupo seleccionado (solo en modo general).
     */
    fun seleccionarGrupo(grupoId: String?) {
        _uiState.value = _uiState.value.copy(grupoSeleccionado = grupoId)
        aplicarFiltros()
    }

    /**
     * Cambia el filtro de tipo de contenido.
     */
    fun cambiarFiltroTipo(filtro: FiltroTipo) {
        _uiState.value = _uiState.value.copy(filtroTipo = filtro)
        aplicarFiltros()
    }
}

/**
 * Estado de UI para la pantalla de matches.
 */
data class MatchesUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val matches: List<MatchConGrupoConUsuarios> = emptyList(),
    val matchesVacios: Boolean = false,

    // Modo de visualización
    val modoGrupoEspecifico: Boolean = false, // true = un solo grupo, false = todos los grupos

    // Filtros
    val grupoSeleccionado: String? = null, // null = todos los grupos
    val filtroTipo: FiltroTipo = FiltroTipo.TODOS,
    val gruposDisponibles: List<GrupoInfo> = emptyList()
)

/**
 * Match con información completa del grupo y usuarios.
 */
data class MatchConGrupoConUsuarios(
    val match: Match,
    val grupoId: String,
    val grupoNombre: String,
    val usuariosInfo: List<Usuario>
) {
    /**
     * Verifica si todos los usuarios coincidieron (unanimidad).
     */
    fun esUnanimidad(): Boolean {
        // Asumimos que si todos los usuarios que votaron coincidieron, es unanimidad
        // En una implementación más completa, necesitaríamos saber el total de miembros del grupo
        return usuariosInfo.size >= 2 // Al menos 2 personas coincidieron
    }
}

/**
 * Información básica de un grupo para los filtros.
 */
data class GrupoInfo(
    val id: String,
    val nombre: String
)

/**
 * Tipos de filtro para matches.
 */
enum class FiltroTipo {
    TODOS,
    PELICULAS,
    SERIES
}
