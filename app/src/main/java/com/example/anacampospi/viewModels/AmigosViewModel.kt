package com.example.anacampospi.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anacampospi.modelo.SolicitudAmistad
import com.example.anacampospi.modelo.Usuario
import com.example.anacampospi.repositorio.AmigoRepository
import com.example.anacampospi.repositorio.GrupoRepository
import com.example.anacampospi.repositorio.SolicitudAmistadRepository
import com.example.anacampospi.repositorio.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la búsqueda y gestión de amigos.
 */
class AmigosViewModel(
    private val usuarioRepository: UsuarioRepository = UsuarioRepository(),
    private val amigoRepository: AmigoRepository = AmigoRepository(),
    private val solicitudRepository: SolicitudAmistadRepository = SolicitudAmistadRepository(),
    private val grupoRepository: GrupoRepository = GrupoRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AmigosUiState())
    val uiState: StateFlow<AmigosUiState> = _uiState.asStateFlow()

    private var solicitudesListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        cargarDatosUsuario()
        cargarAmigos()
        escucharSolicitudesPendientes() // Cambiado a listener en tiempo real
        cargarSolicitudesEnviadas()
    }

    override fun onCleared() {
        super.onCleared()
        solicitudesListener?.remove()
    }

    /**
     * Carga el código de invitación del usuario actual.
     */
    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val result = usuarioRepository.getUsuario(uid)
            result.onSuccess { usuario ->
                _uiState.value = _uiState.value.copy(
                    codigoPropio = usuario.codigoInvitacion,
                    nombreUsuario = usuario.nombre
                )
            }
        }
    }

    /**
     * Carga la lista de amigos del usuario actual.
     */
    fun cargarAmigos() {
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
     * Busca un usuario por su código de invitación.
     */
    fun buscarPorCodigo(codigo: String) {
        if (codigo.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorBusqueda = "Introduce un código válido"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                buscando = true,
                errorBusqueda = null,
                usuarioEncontrado = null
            )

            val usuario = usuarioRepository.buscarPorCodigo(codigo)

            if (usuario != null) {
                // Verificar si es el usuario actual
                if (usuario.idUsuario == auth.currentUser?.uid) {
                    _uiState.value = _uiState.value.copy(
                        buscando = false,
                        errorBusqueda = "No puedes añadirte a ti mismo"
                    )
                    return@launch
                }

                // Verificar si ya son amigos
                val uid = auth.currentUser?.uid ?: return@launch
                val yaAmigos = amigoRepository.sonAmigos(uid, usuario.idUsuario)

                _uiState.value = _uiState.value.copy(
                    buscando = false,
                    usuarioEncontrado = usuario,
                    yaSonAmigos = yaAmigos
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    buscando = false,
                    errorBusqueda = "No se encontró ningún usuario con ese código"
                )
            }
        }
    }

    /**
     * Envía una solicitud de amistad al usuario encontrado.
     */
    fun añadirAmigo() {
        val uid = auth.currentUser?.uid ?: return
        val uidAmigo = _uiState.value.usuarioEncontrado?.idUsuario ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(añadiendoAmigo = true, errorBusqueda = null)

            val result = solicitudRepository.enviarSolicitud(uid, uidAmigo)

            result.onSuccess { mensaje ->
                _uiState.value = _uiState.value.copy(
                    añadiendoAmigo = false,
                    mensajeExito = if (mensaje.contains("automáticamente")) {
                        "¡Ya sois amigos!"
                    } else {
                        "¡Solicitud enviada!"
                    },
                    usuarioEncontrado = null,
                    yaSonAmigos = false
                )
                cargarAmigos() // Recargar lista de amigos
                cargarSolicitudesEnviadas() // Recargar solicitudes enviadas
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    añadiendoAmigo = false,
                    errorBusqueda = error.message
                )
            }
        }
    }

    /**
     * Escucha las solicitudes de amistad pendientes en tiempo real.
     */
    private fun escucharSolicitudesPendientes() {
        val uid = auth.currentUser?.uid ?: return

        android.util.Log.d("AmigosViewModel", "Configurando listener de solicitudes para: $uid")

        viewModelScope.launch {
            solicitudesListener = solicitudRepository.escucharSolicitudesPendientes(uid) { solicitudes ->
                android.util.Log.d("AmigosViewModel", "Solicitudes actualizadas: ${solicitudes.size}")
                _uiState.value = _uiState.value.copy(
                    solicitudesPendientes = solicitudes
                )
            }
        }
    }

    /**
     * Carga las solicitudes enviadas que están pendientes.
     */
    private fun cargarSolicitudesEnviadas() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val result = solicitudRepository.obtenerSolicitudesEnviadas(uid)
            result.onSuccess { solicitudes ->
                _uiState.value = _uiState.value.copy(
                    solicitudesEnviadas = solicitudes
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorAmigos = "Error al cargar solicitudes enviadas: ${error.message}"
                )
            }
        }
    }

    /**
     * Acepta una solicitud de amistad.
     */
    fun aceptarSolicitud(solicitudId: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val result = solicitudRepository.aceptarSolicitud(uid, solicitudId)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    mensajeExito = "¡Solicitud aceptada!"
                )
                // Las solicitudes se actualizarán automáticamente por el listener
                cargarAmigos()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorAmigos = error.message
                )
            }
        }
    }

    /**
     * Rechaza una solicitud de amistad.
     */
    fun rechazarSolicitud(solicitudId: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val result = solicitudRepository.rechazarSolicitud(uid, solicitudId)
            result.onSuccess {
                // Las solicitudes se actualizarán automáticamente por el listener
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorAmigos = error.message
                )
            }
        }
    }

    /**
     * Cancela una solicitud enviada.
     */
    fun cancelarSolicitud(solicitudId: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val result = solicitudRepository.cancelarSolicitud(uid, solicitudId)
            result.onSuccess {
                cargarSolicitudesEnviadas()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorAmigos = error.message
                )
            }
        }
    }

    /**
     * Elimina un amigo de la lista.
     * Verifica primero si el amigo está en alguna ronda activa.
     */
    fun eliminarAmigo(uidAmigo: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Verificar si el amigo está en rondas activas
            val gruposResult = grupoRepository.obtenerGruposDelUsuario(uid)

            gruposResult.onSuccess { grupos ->
                // Verificar si el amigo está en algún grupo
                val gruposConAmigo = grupos.filter { grupo ->
                    grupo.miembros.contains(uidAmigo) &&
                    grupo.estado != "FINALIZADA" // Verificar que no esté finalizada
                }

                if (gruposConAmigo.isNotEmpty()) {
                    // No permitir eliminar si está en rondas activas
                    _uiState.value = _uiState.value.copy(
                        errorAmigos = "No puedes eliminar a este amigo porque está en ${gruposConAmigo.size} ${if (gruposConAmigo.size == 1) "ronda activa" else "rondas activas"}"
                    )
                } else {
                    // Proceder con la eliminación
                    val result = amigoRepository.eliminarAmigo(uid, uidAmigo)

                    result.onSuccess {
                        _uiState.value = _uiState.value.copy(
                            mensajeExito = "Amigo eliminado correctamente"
                        )
                        cargarAmigos() // Recargar lista
                    }.onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            errorAmigos = error.message
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorAmigos = "Error al verificar rondas: ${error.message}"
                )
            }
        }
    }

    /**
     * Limpia los mensajes de error y éxito.
     */
    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(
            errorBusqueda = null,
            errorAmigos = null,
            mensajeExito = null
        )
    }

    /**
     * Limpia el resultado de búsqueda.
     */
    fun limpiarBusqueda() {
        _uiState.value = _uiState.value.copy(
            usuarioEncontrado = null,
            errorBusqueda = null,
            yaSonAmigos = false
        )
    }

    /**
     * Limpia el error de amigos.
     */
    fun limpiarErrorAmigos() {
        _uiState.value = _uiState.value.copy(errorAmigos = null)
    }
}

/**
 * Estado de la UI para la pantalla de amigos.
 */
data class AmigosUiState(
    val codigoPropio: String = "",
    val nombreUsuario: String = "",

    // Búsqueda de usuarios
    val buscando: Boolean = false,
    val usuarioEncontrado: Usuario? = null,
    val errorBusqueda: String? = null,
    val yaSonAmigos: Boolean = false,
    val añadiendoAmigo: Boolean = false,

    // Lista de amigos
    val amigos: List<Usuario> = emptyList(),
    val cargandoAmigos: Boolean = false,
    val errorAmigos: String? = null,

    // Solicitudes de amistad
    val solicitudesPendientes: List<Pair<SolicitudAmistad, Usuario>> = emptyList(), // Recibidas
    val solicitudesEnviadas: List<Pair<SolicitudAmistad, Usuario>> = emptyList(), // Enviadas

    // Mensajes
    val mensajeExito: String? = null
)
