package com.example.anacampospi.util

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Utilidad para verificar el estado de la aplicación.
 * Usa ProcessLifecycleOwner para detectar de manera confiable
 * si la app está en primer plano o en segundo plano.
 */
object AppStateUtil : LifecycleObserver {

    private var isInForeground = false

    init {
        // Registrar observador del ciclo de vida del proceso
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * Se llama cuando la app pasa a primer plano
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        isInForeground = true
        android.util.Log.d("AppStateUtil", "App entró en PRIMER PLANO")
    }

    /**
     * Se llama cuando la app pasa a segundo plano
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        isInForeground = false
        android.util.Log.d("AppStateUtil", "App entró en SEGUNDO PLANO")
    }

    /**
     * Verifica si la aplicación está en primer plano.
     * @param context No se usa en esta implementación, pero se mantiene
     *                para compatibilidad con el código existente.
     * @return true si la app está visible (en primer plano), false si no.
     */
    fun isAppInForeground(context: Context): Boolean {
        return isInForeground
    }
}
