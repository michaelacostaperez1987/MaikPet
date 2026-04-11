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
import com.example.pet.data.model.Mascota
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
            val lastId = getLastMascotaId()
            
            val response = api.getMascotas()
            if (response.isSuccessful) {
                val mascotas = response.body() ?: emptyList()
                
                val newestMascota = mascotas.maxByOrNull { it.id }
                if (newestMascota != null && newestMascota.id > lastId && lastId > 0) {
                    showNewMascotaNotification(newestMascota)
                }
                
                setLastMascotaId(newestMascota?.id ?: 0)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun getLastMascotaId(): Int {
        return context.getSharedPreferences("check_prefs", Context.MODE_PRIVATE)
            .getInt("last_mascota_id", 0)
    }

    private fun setLastMascotaId(id: Int) {
        context.getSharedPreferences("check_prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt("last_mascota_id", id)
            .apply()
    }

    private fun showNewMascotaNotification(mascota: Mascota) {
        val channelId = "maikpet_new_mascotas"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nuevas mascotas",
                NotificationManager.IMPORTANCE_HIGH
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

        val emoji = if (mascota.tipo == "Perro") "🐕" else "🐱"
        val title = "$emoji ${mascota.nombre} busca hogar!"
        val body = "${mascota.tipo} - ${mascota.edadMeses} meses${if (mascota.vacunas == "Si") " - Vacunado" else ""}"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$emoji ${mascota.nombre} está buscando un hogar!\n\n" +
                        "Tipo: ${mascota.tipo}\n" +
                        "Edad: ${mascota.edadMeses} meses\n" +
                        "Vacunas: ${mascota.vacunas}\n" +
                        "Ubicación: ${mascota.direccion}\n\n" +
                        "Toca para ver en el mapa"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
