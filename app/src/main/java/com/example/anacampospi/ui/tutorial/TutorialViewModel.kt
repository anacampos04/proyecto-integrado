package com.example.anacampospi.ui.tutorial

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.anacampospi.util.TutorialPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel para gestionar el estado del tutorial de onboarding
 */
class TutorialViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TutorialUiState())
    val uiState: StateFlow<TutorialUiState> = _uiState.asStateFlow()

    private val steps = TutorialSteps.steps

    init {
        // Inicializar con el primer paso
        _uiState.value = _uiState.value.copy(
            pasoActual = steps[0],
            pasoIndex = 0,
            totalPasos = steps.size
        )
    }

    /**
     * Verifica si el tutorial debe mostrarse
     */
    fun deberMostrarTutorial(context: Context): Boolean {
        val prefs = TutorialPreferences(context)
        return !prefs.isTutorialCompletado()
    }

    /**
     * Avanza al siguiente paso
     */
    fun siguientePaso() {
        val nextIndex = _uiState.value.pasoIndex + 1
        if (nextIndex < steps.size) {
            _uiState.value = _uiState.value.copy(
                pasoActual = steps[nextIndex],
                pasoIndex = nextIndex
            )
        }
    }

    /**
     * Retrocede al paso anterior
     */
    fun pasoAnterior() {
        val prevIndex = _uiState.value.pasoIndex - 1
        if (prevIndex >= 0) {
            _uiState.value = _uiState.value.copy(
                pasoActual = steps[prevIndex],
                pasoIndex = prevIndex
            )
        }
    }

    /**
     * Salta el tutorial
     */
    fun saltarTutorial(context: Context) {
        val prefs = TutorialPreferences(context)
        prefs.marcarTutorialCompletado()
        _uiState.value = _uiState.value.copy(tutorialActivo = false)
    }

    /**
     * Completa el tutorial
     */
    fun completarTutorial(context: Context) {
        val prefs = TutorialPreferences(context)
        prefs.marcarTutorialCompletado()
        _uiState.value = _uiState.value.copy(tutorialActivo = false)
    }

    /**
     * Muestra el tutorial
     */
    fun mostrarTutorial() {
        _uiState.value = _uiState.value.copy(tutorialActivo = true)
    }

    /**
     * Indica si es el último paso
     */
    fun esUltimoPaso(): Boolean {
        return _uiState.value.pasoIndex == steps.size - 1
    }

    /**
     * Indica si es el primer paso
     */
    fun esPrimerPaso(): Boolean {
        return _uiState.value.pasoIndex == 0
    }
}

/**
 * Estado de UI del tutorial
 */
data class TutorialUiState(
    val tutorialActivo: Boolean = false,
    val pasoActual: TutorialStep? = null,
    val pasoIndex: Int = 0,
    val totalPasos: Int = 0
)
