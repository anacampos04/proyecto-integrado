package com.example.anacampospi.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anacampospi.modelo.Usuario
import com.example.anacampospi.repositorio.AuthRepository
import com.example.anacampospi.repositorio.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PerfilUiState(
    val loading: Boolean = false,
    val usuario: Usuario? = null,
    val error: String? = null
)

class PerfilViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    /**
     * Carga los datos del perfil del usuario
     */
    private fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)

            val uid = authRepository.currentUid()
            if (uid == null) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "No hay sesión activa"
                )
                return@launch
            }

            val resultado = usuarioRepository.getUsuario(uid)

            resultado.onSuccess { usuario ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    usuario = usuario,
                    error = null
                )
            }

            resultado.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = error.message
                )
            }
        }
    }

    /**
     * Actualiza las plataformas del usuario
     */
    fun actualizarPlataformas(plataformas: List<String>) {
        viewModelScope.launch {
            val uid = authRepository.currentUid() ?: return@launch

            val resultado = usuarioRepository.actualizarPlataformas(uid, plataformas)

            resultado.onSuccess {
                // Recargar perfil para mostrar cambios
                cargarPerfil()
                android.util.Log.d("PerfilViewModel", "Plataformas actualizadas: $plataformas")
            }

            resultado.onFailure { error ->
                android.util.Log.e("PerfilViewModel", "Error actualizando plataformas", error)
                _uiState.value = _uiState.value.copy(
                    error = error.message
                )
            }
        }
    }

    /**
     * Actualiza el nombre del usuario
     */
    fun actualizarNombre(nuevoNombre: String) {
        viewModelScope.launch {
            val uid = authRepository.currentUid() ?: return@launch

            val resultado = usuarioRepository.actualizarNombre(uid, nuevoNombre)

            resultado.onSuccess {
                cargarPerfil() // Recargar para mostrar cambios
                android.util.Log.d("PerfilViewModel", "Nombre actualizado: $nuevoNombre")
            }

            resultado.onFailure { error ->
                android.util.Log.e("PerfilViewModel", "Error actualizando nombre", error)
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }

    /**
     * Actualiza la región del usuario
     */
    fun actualizarRegion(nuevaRegion: String) {
        viewModelScope.launch {
            val uid = authRepository.currentUid() ?: return@launch

            try {
                usuarioRepository.actualizarRegion(uid, nuevaRegion)
                cargarPerfil() // Recargar para mostrar cambios
                android.util.Log.d("PerfilViewModel", "Región actualizada: $nuevaRegion")
            } catch (e: Exception) {
                android.util.Log.e("PerfilViewModel", "Error actualizando región", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Cierra la sesión del usuario
     */
    fun cerrarSesion() {
        authRepository.logout()
    }
}
