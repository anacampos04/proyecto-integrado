package com.example.anacampospi.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.anacampospi.MainActivity
import com.example.anacampospi.R
import com.example.anacampospi.repositorio.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Servicio de Firebase Cloud Messaging para manejar notificaciones push
 */
class PopcornFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val usuarioRepository = UsuarioRepository()

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "popcorn_notifications"
        private const val CHANNEL_NAME = "Notificaciones de Rondas"
    }

    /**
     * Se llama cuando se recibe una nueva notificación push
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Mensaje recibido de: ${remoteMessage.from}")

        // Si el mensaje tiene datos
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Si el mensaje tiene notificación
        remoteMessage.notification?.let {
            Log.d(TAG, "Título: ${it.title}, Mensaje: ${it.body}")
            mostrarNotificacion(
                titulo = it.title ?: "PopcornTribu",
                mensaje = it.body ?: "",
                data = remoteMessage.data
            )
        }
    }

    /**
     * Se llama cuando se genera un nuevo token FCM o cuando se actualiza
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Nuevo token FCM: $token")

        // Guardar el token en Firestore para el usuario actual
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            serviceScope.launch {
                usuarioRepository.actualizarTokenFCM(uid, token)
            }
        }
    }

    /**
     * Maneja los datos personalizados del mensaje
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val tipo = data["tipo"]
        val grupoId = data["grupoId"]
        val nombreGrupo = data["nombreGrupo"]

        when (tipo) {
            "invitacion_ronda" -> {
                mostrarNotificacion(
                    titulo = "Nueva fiesta 🎉",
                    mensaje = "Te han invitado a '$nombreGrupo'. ¡Configura tus preferencias!",
                    data = data
                )
            }
            "ronda_activada" -> {
                mostrarNotificacion(
                    titulo = "¡Fiesta lista! 🎉",
                    mensaje = "'$nombreGrupo' está lista. ¡Empieza a hacer swipe!",
                    data = data
                )
            }
            "nuevo_match" -> {
                val titulo = data["tituloContenido"] ?: "Nuevo match"
                mostrarNotificacion(
                    titulo = "¡Match!",
                    mensaje = "A tu grupo le gusta '$titulo'",
                    data = data
                )
            }
            "peticion_amistad" -> {
                // La notificación ya viene con título y mensaje correctos
                // desde Cloud Functions, solo necesitamos mostrarla
                mostrarNotificacion(
                    titulo = "Nueva solicitud de amistad",
                    mensaje = data["mensaje"] ?: "Tienes una nueva solicitud de amistad",
                    data = data
                )
            }
        }
    }

    /**
     * Muestra una notificación local en el dispositivo
     */
    private fun mostrarNotificacion(
        titulo: String,
        mensaje: String,
        data: Map<String, String> = emptyMap()
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificaciones para Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // Importante para heads-up
            ).apply {
                description = "Notificaciones sobre rondas de swipe y matches"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                // Sonido predeterminado para que sea más visible
                setSound(
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la app cuando se toca la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Pasar datos extras para navegación
            // Usar las mismas claves que Firebase usa automáticamente en background
            Log.d(TAG, "Creando intent con data: $data")
            data["tipo"]?.let { tipo ->
                Log.d(TAG, "Añadiendo extra tipo: $tipo")
                putExtra("tipo", tipo)
            } ?: Log.w(TAG, "data[tipo] es null!")
            data["grupoId"]?.let { putExtra("grupoId", it) }
            data["solicitudId"]?.let { putExtra("solicitudId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construir la notificación con configuración para heads-up
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Máxima prioridad
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Categoría de mensaje para heads-up
            .setAutoCancel(true) // Se elimina al tocar
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250)) // Patrón de vibración
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI) // Sonido
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sonido, vibración y luz por defecto
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible en pantalla de bloqueo
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
