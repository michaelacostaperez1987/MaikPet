package com.macosta.maikpet.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

object NotificationUtils {
    const val CHANNEL_GENERAL = "maikpet_channel"
    const val CHANNEL_NEW_MASCOTAS = "maikpet_new_mascotas"

    fun createChannel(
        notificationManager: NotificationManager,
        channelId: String,
        channelName: String,
        description: String,
        importance: Int = NotificationManager.IMPORTANCE_HIGH
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                importance
            ).apply {
                this.description = description
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
