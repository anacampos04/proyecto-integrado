package com.example.anacampospi.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anacampospi.repositorio.AuthRepository
import com.example.anacampospi.repositorio.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SetupUiState(
    val plataformasSeleccionadas: Set<String> = emptySet(),
    val guardando: Boolean = false,
    val completado: Boolean = false,
    val error: String? = null
)

class SetupViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val usuarioRepo: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(SetupUiState())
    val state = _state.asStateFlow()

    /**
     * Alterna la selección de una plataforma
     */
    fun togglePlataforma(plataformaId: String) {
        val current = _state.value.plataformasSeleccionadas
        _state.value = _state.value.copy(
            plataformasSeleccionadas = if (current.contains(plataformaId)) {
                current - plataformaId
            } else {
                current + plataformaId
            },
            error = null
        )
    }

    /**
     * Guarda la configuración de plataformas en Firestore
     */
    fun guardarConfiguracion() {
        val uid = authRepo.currentUid()
        if (uid == null) {
            _state.value = _state.value.copy(error = "Usuario no autenticado")
            return
        }

        if (_state.value.plataformasSeleccionadas.isEmpty()) {
            _state.value = _state.value.copy(error = "Selecciona al menos una plataforma")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(guardando = true, error = null)

            try {
                usuarioRepo.actualizarPlataformas(
                    uid = uid,
                    plataformas = _state.value.plataformasSeleccionadas.toList()
                )

                _state.value = _state.value.copy(
                    guardando = false,
                    completado = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    guardando = false,
                    error = "Error al guardar: ${e.message}"
                )
            }
        }
    }
}