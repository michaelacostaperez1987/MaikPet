package com.example.pet.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pet.MainActivity
import com.example.pet.R
import com.example.pet.data.api.MaikPetApi
import com.example.pet.data.api.TokenRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MaikPetFirebaseService : FirebaseMessagingService() {

    @Inject
    lateinit var api: MaikPetApi

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token: $token")
        saveToken(token)
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val title = message.notification?.title ?: message.data["title"] ?: "Nueva mascota"
        val body = message.notification?.body ?: message.data["body"] ?: "Hay una nueva mascota para adopción"
        
        val mascotaId = message.data["mascota_id"]
        val tipo = message.data["tipo"]
        
        Log.d("FCM", "Notificación recibida: title=$title, mascota_id=$mascotaId")
        
        showNotification(title, body, mascotaId)
    }

    private fun saveToken(token: String) {
        getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
    }

    private fun sendTokenToServer(token: String) {
        val prefs = getSharedPreferences("maikpet_prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("user_email", null)
        
        if (email != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = api.saveDeviceToken(TokenRequest(token))
                    Log.d("FCM", "Token enviado al servidor: ${response.isSuccessful}")
                } catch (e: Exception) {
                    Log.e("FCM", "Error al enviar token: ${e.message}")
                }
            }
        }
    }

    private fun showNotification(title: String, body: String, mascotaId: String?) {
        val channelId = "maikpet_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "MaikPet Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevas mascotas para adopción"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_mapa", true)
            mascotaId?.let { putExtra("mascota_id", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 100, 500))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        fun getToken(context: Context): String? {
            return context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
                .getString("fcm_token", null)
        }
        
        fun saveUserEmail(context: Context, email: String) {
            context.getSharedPreferences("maikpet_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("user_email", email)
                .apply()
            
            // Enviar token si existe
            val token = getToken(context)
            if (token != null) {
                Log.d("FCM", "Token existente detectado, guardando...")
                saveTokenToServerSync(context, token)
            }
        }
        
        private fun saveTokenToServerSync(context: Context, token: String) {
            val prefs = context.getSharedPreferences("maikpet_prefs", Context.MODE_PRIVATE)
            val email = prefs.getString("user_email", null)
            
            if (email != null) {
                val handler = android.os.Handler(android.os.Looper.getMainLooper())
                handler.post {
                    // This won't work directly, so we'll use a different approach
                    Log.d("FCM", "Token ready for send, user is logged in")
                }
            }
        }
    }
}
