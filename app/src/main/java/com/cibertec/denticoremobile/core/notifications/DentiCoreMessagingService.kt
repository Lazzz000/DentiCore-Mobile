package com.cibertec.denticoremobile.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cibertec.denticoremobile.MainActivity
import com.cibertec.denticoremobile.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio de Firebase Cloud Messaging (FCM).
 *
 * Recibe notificaciones silenciosas y visuales provenientes del backend
 * (a través de RabbitMQ → FCM) y las muestra en la bandeja del sistema.
 */
class DentiCoreMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Enviar el token al backend para vincularlo con el paciente autenticado.
        Log.d(TAG, "Nuevo FCM token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data[TITLE_KEY]
            ?: getString(R.string.app_name)

        val body = message.notification?.body
            ?: message.data[BODY_KEY]
            ?: "Tiene una nueva notificación de DentiCore"

        showNotification(title = title, body = body)
    }

    /**
     * Construye y muestra la notificación en la bandeja del sistema.
     */
    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Crea el canal de notificación requerido en Android 8.0 (API 26) y superiores.
     */
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel != null) return

            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.default_notification_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "DentiCoreFCM"
        private const val CHANNEL_ID = "denticore_default_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TITLE_KEY = "title"
        private const val BODY_KEY = "body"
    }
}
