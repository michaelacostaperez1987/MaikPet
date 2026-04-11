package com.example.pet.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.pet.MainActivity
import com.example.pet.R
import com.example.pet.data.api.MaikPetApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class CheckNewMascotasWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: MaikPetApi
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val currentTime = System.currentTimeMillis()
            val lastCount = getLastMascotaCount()
            
            val response = api.getMascotas()
            if (response.isSuccessful) {
                val mascotas = response.body() ?: emptyList()
                val currentCount = mascotas.size
                
                if (currentCount > lastCount && lastCount > 0) {
                    val newCount = currentCount - lastCount
                    showNewMascotasNotification(newCount)
                }
                
                setLastMascotaCount(currentCount)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun getLastMascotaCount(): Int {
        return context.getSharedPreferences("check_prefs", Context.MODE_PRIVATE)
            .getInt("last_mascota_count", 0)
    }

    private fun setLastMascotaCount(count: Int) {
        context.getSharedPreferences("check_prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt("last_mascota_count", count)
            .apply()
    }

    private fun showNewMascotasNotification(count: Int) {
        val channelId = "maikpet_new_mascotas"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nuevas mascotas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de nuevas mascotas en adopción"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_mapa", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (count == 1) "Nueva mascota en adopción" else "$count nuevas mascotas en adopción"
        val body = "Hay mascotas esperando un nuevo hogar. ¡Échales un vistazo!"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "check_new_mascotas"
        private const val NOTIFICATION_ID = 1001

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<CheckNewMascotasWorker>(
                3, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
