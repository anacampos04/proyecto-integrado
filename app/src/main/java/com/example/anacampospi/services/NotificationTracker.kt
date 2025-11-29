package com.example.anacampospi.services

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper para rastrear qué notificaciones de match ya han sonado.
 * Las notificaciones solo sonarán una vez hasta que el usuario abra la app.
 */
object NotificationTracker {

    private const val PREFS_NAME = "notification_tracker"
    private const val KEY_NOTIFIED_MATCHES = "notified_matches"

    /**
     * Obtiene las SharedPreferences
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Genera una clave única para un match (grupo + contenido)
     */
    private fun getMatchKey(grupoId: String, contentId: String?): String {
        return if (contentId != null) {
            "match_${grupoId}_${contentId}"
        } else {
            "match_${grupoId}"
        }
    }

    /**
     * Verifica si un match ya ha sido notificado (ya ha sonado)
     */
    fun hasBeenNotified(context: Context, grupoId: String, contentId: String? = null): Boolean {
        val prefs = getPrefs(context)
        val notifiedMatches = prefs.getStringSet(KEY_NOTIFIED_MATCHES, emptySet()) ?: emptySet()
        val key = getMatchKey(grupoId, contentId)
        return notifiedMatches.contains(key)
    }

    /**
     * Marca un match como notificado (para que no vuelva a sonar)
     */
    fun markAsNotified(context: Context, grupoId: String, contentId: String? = null) {
        val prefs = getPrefs(context)
        val notifiedMatches = prefs.getStringSet(KEY_NOTIFIED_MATCHES, emptySet())?.toMutableSet() ?: mutableSetOf()
        val key = getMatchKey(grupoId, contentId)
        notifiedMatches.add(key)
        prefs.edit().putStringSet(KEY_NOTIFIED_MATCHES, notifiedMatches).apply()
    }

    /**
     * Limpia todos los matches notificados (para que puedan volver a sonar).
     * Se debe llamar cuando el usuario abre la pantalla de matches.
     */
    fun clearAllNotifications(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().remove(KEY_NOTIFIED_MATCHES).apply()
    }

    /**
     * Limpia solo las notificaciones de un grupo específico
     */
    fun clearGroupNotifications(context: Context, grupoId: String) {
        val prefs = getPrefs(context)
        val notifiedMatches = prefs.getStringSet(KEY_NOTIFIED_MATCHES, emptySet())?.toMutableSet() ?: mutableSetOf()
        val filtered = notifiedMatches.filter { !it.startsWith("match_${grupoId}_") }.toSet()
        prefs.edit().putStringSet(KEY_NOTIFIED_MATCHES, filtered).apply()
    }
}
