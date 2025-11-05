package com.example.anacampospi.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anacampospi.modelo.Grupo
import com.example.anacampospi.modelo.Usuario
import com.example.anacampospi.modelo.enums.TipoContenido
import com.example.anacampospi.repositorio.AmigoRepository
import com.example.anacampospi.repositorio.GrupoRepository
import com.example.anacampospi.repositorio.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la configuración de ronda.
 * Soporta dos modos:
 * - Creador: crear nueva ronda con amigos, plataformas, tipos y géneros
 * - Invitado: configurar solo plataformas y géneros en ronda existente
 */
class ConfiguracionRondaViewModel(
    private val amigoRepository: AmigoRepository = AmigoRepository(),
    private val grupoRepository: GrupoRepository = GrupoRepository(),
    private val usuarioRepository: UsuarioRepository= UsuarioRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfiguracionRondaUiState())
    val uiState: StateFlow<ConfiguracionRondaUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el ViewModel.
     * Si grupoId es null, modo creador (carga amigos).
     * Si grupoId no es null, modo invitado (carga grupo).
     */
    fun inicializar(grupoId: String?) {
        if (grupoId == null) {
            // Modo creador
            cargarAmigos()
            cargarPlataformasUsuario()
            _uiState.value = _uiState.value.copy(esInvitado = false, grupoId = null)
        } else {
            // Modo invitado
            cargarGrupo(grupoId)
            cargarPlataformasUsuario()
            _uiState.value = _uiState.value.copy(esInvitado = true, grupoId = grupoId)
        }
    }

    /**
     * Carga la lista de amigos del usuario.
     */
    private fun cargarAmigos() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargandoAmigos = true)

            val result = amigoRepository.obtenerAmigos(uid)
            result.onSuccess { amigos ->
                _uiState.value = _uiState.value.copy(
                    amigos = amigos,
                    cargandoAmigos = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorAmigos = error.message,
                    cargandoAmigos = false
                )
            }
        }
    }

    /**
     * Crea un nuevo grupo con los parámetros especificados.
     * @param nombrePersonalizado Nombre personalizado opcional para el grupo
     * @return ID del grupo creado o null si hubo error
     */
    suspend fun crearGrupo(
        nombrePersonalizado: String? = null,
        amigosSeleccionados: List<String>,
        plataformas: List<String>,
        tipos: List<TipoContenido>, // Puede ser uno o ambos
        generos: List<Int>
    ): String? {
        val uid = auth.currentUser?.uid ?: return null

        _uiState.value = _uiState.value.copy(creandoGrupo = true)

        val result = grupoRepository.crearGrupo(
            creadoPor = uid,
            miembros = amigosSeleccionados, // NO incluir al creador, el repo lo hace
            nombrePersonalizado = nombrePersonalizado,
            plataformas = plataformas,
            tipos = tipos,
            generos = generos
        )

        _uiState.value = _uiState.value.copy(creandoGrupo = false)

        return result.getOrNull()
    }

    /**
     * Configura la ronda cuando el usuario es invitado.
     */
    suspend fun configurarRonda(
        idGrupo: String,
        plataformas: List<String>,
        generos: List<Int>
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

        _uiState.value = _uiState.value.copy(creandoGrupo = true)

        val result = grupoRepository.configurarRonda(
            idGrupo = idGrupo,
            uid = uid,
            plataformas = plataformas,
            generos = generos
        )

        _uiState.value = _uiState.value.copy(creandoGrupo = false)

        return result
    }

    /**
     * Verifica el estado actual del grupo (para saber si quedó ACTIVA después de configurar).
     */
    suspend fun verificarEstadoGrupo(idGrupo: String): Grupo? {
        return grupoRepository.obtenerGrupo(idGrupo).getOrNull()
    }

    /**
     * Carga los datos del grupo cuando el usuario es invitado.
     */
    private fun cargarGrupo(grupoId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargandoGrupo = true)

            val result = grupoRepository.obtenerGrupo(grupoId)
            result.onSuccess { grupo ->
                _uiState.value = _uiState.value.copy(
                    grupo = grupo,
                    cargandoGrupo = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorGrupo = error.message,
                    cargandoGrupo = false
                )
            }
        }
    }

    /**
     * Carga las plataformas configuradas del usuario para pre-seleccionarlas.
     */
    private fun cargarPlataformasUsuario() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val result = usuarioRepository.getUsuario(uid)
            result.onSuccess { usuario ->
                _uiState.value = _uiState.value.copy(
                    plataformasUsuario = usuario.plataformas
                )
            }
        }
    }
}

/**
 * Estado de la UI para configuración de ronda.
 */
data class ConfiguracionRondaUiState(
    val esInvitado: Boolean = false, // true si está configurando como invitado
    val grupoId: String? = null, // ID del grupo si es invitado
    val grupo: Grupo? = null, // Datos del grupo si es invitado
    val cargandoGrupo: Boolean = false,
    val errorGrupo: String? = null,
    val amigos: List<Usuario> = emptyList(),
    val cargandoAmigos: Boolean = false,
    val errorAmigos: String? = null,
    val plataformasUsuario: List<String> = emptyList(), // Plataformas del usuario para pre-seleccionar
    val creandoGrupo: Boolean = false
)
