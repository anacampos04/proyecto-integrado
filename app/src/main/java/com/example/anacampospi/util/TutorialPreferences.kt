package com.example.anacampospi.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper para gestionar las preferencias del tutorial de onboarding
 */
class TutorialPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "tutorial_prefs"
        private const val KEY_TUTORIAL_COMPLETADO = "tutorial_completado"
    }

    /**
     * Verifica si el tutorial ya fue completado
     */
    fun isTutorialCompletado(): Boolean {
        return prefs.getBoolean(KEY_TUTORIAL_COMPLETADO, false)
    }

    /**
     * Marca el tutorial como completado
     */
    fun marcarTutorialCompletado() {
        prefs.edit().putBoolean(KEY_TUTORIAL_COMPLETADO, true).apply()
    }

    /**
     * Reinicia el tutorial (útil para testing)
     */
    fun resetTutorial() {
        prefs.edit().putBoolean(KEY_TUTORIAL_COMPLETADO, false).apply()
    }
}
