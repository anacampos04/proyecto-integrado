package com.example.anacampospi.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anacampospi.modelo.Grupo
import com.example.anacampospi.repositorio.GrupoRepository
import com.example.anacampospi.repositorio.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla Home que muestra las rondas activas.
 */
class HomeViewModel(
    private val grupoRepository: GrupoRepository = GrupoRepository(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarDatosUsuario()
        cargarGrupos()
    }

    /**
     * Carga el nombre del usuario actual.
     */
    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val result = usuarioRepository.getUsuario(uid)
            result.onSuccess { usuario ->
                _uiState.value = _uiState.value.copy(
                    nombreUsuario = usuario.nombre
                )
            }
        }
    }

    /**
     * Carga todos los grupos del usuario separados por estado.
     */
    fun cargarGrupos() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)

            // Cargar los tres tipos de rondas en paralelo
            val rondasActivasResult = grupoRepository.obtenerRondasActivas(uid)
            val rondasPendientesResult = grupoRepository.obtenerRondasPendientes(uid)
            val rondasEsperandoResult = grupoRepository.obtenerRondasEsperando(uid)

            val activas = rondasActivasResult.getOrNull() ?: emptyList()
            val pendientes = rondasPendientesResult.getOrNull() ?: emptyList()
            val esperando = rondasEsperandoResult.getOrNull() ?: emptyList()

            // Ordenar rondas: pendientes primero, luego esperando, luego activas (más recientes primero)
            val activasOrdenadas = activas.sortedByDescending { it.creadoEn }
            val pendientesOrdenadas = pendientes.sortedByDescending { it.creadoEn }
            val esperandoOrdenadas = esperando.sortedByDescending { it.creadoEn }

            // Prioridad: pendientes > esperando > activas
            // Pendientes = rondas en configuración
            // Esperando = rondas donde tu grupo te espera para configurar
            // Activas = rondas listas para hacer swipe
            val gruposOrdenados = pendientesOrdenadas + esperandoOrdenadas + activasOrdenadas

            _uiState.value = _uiState.value.copy(
                rondasActivas = activasOrdenadas,
                rondasPendientes = pendientesOrdenadas,
                rondasEsperando = esperandoOrdenadas,
                grupos = gruposOrdenados, // Orden final: pendientes primero, luego activas recientes, luego esperando
                cargando = false
            )

            // Manejar errores si los hay
            if (rondasActivasResult.isFailure || rondasPendientesResult.isFailure || rondasEsperandoResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar algunas rondas"
                )
            }
        }
    }

    /**
     * Elimina un grupo (cualquier miembro puede eliminarlo).
     */
    fun eliminarGrupo(idGrupo: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val result = grupoRepository.eliminarGrupo(idGrupo, uid)
            result.onSuccess {
                cargarGrupos() // Recargar lista
            }.onFailure { error ->
                // Convertir errores técnicos a mensajes amigables
                val mensajeAmigable = when {
                    error.message?.contains("permission", ignoreCase = true) == true ||
                    error.message?.contains("denied", ignoreCase = true) == true ->
                        "No puedes eliminar esta ronda porque ya tiene votos de otros usuarios"
                    error.message?.contains("not found", ignoreCase = true) == true ->
                        "Esta ronda ya no existe"
                    error.message?.contains("network", ignoreCase = true) == true ->
                        "Error de conexión. Verifica tu internet"
                    else -> "No se pudo eliminar la ronda. Inténtalo de nuevo"
                }

                _uiState.value = _uiState.value.copy(
                    error = mensajeAmigable
                )
            }
        }
    }

    /**
     * Limpia los mensajes de error.
     */
    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * Estado de la UI para la pantalla Home.
 */
data class HomeUiState(
    val nombreUsuario: String = "",
    val rondasActivas: List<Grupo> = emptyList(), // Listas para hacer swipe
    val rondasPendientes: List<Grupo> = emptyList(), // Esperando configuración del usuario
    val rondasEsperando: List<Grupo> = emptyList(), // Usuario configuró, esperando a otros
    val grupos: List<Grupo> = emptyList(), // Todos los grupos (para compatibilidad)
    val cargando: Boolean = false,
    val error: String? = null
)
