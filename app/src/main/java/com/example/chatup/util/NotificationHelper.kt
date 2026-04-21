package com.example.chatup.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.chatup.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "chatup_general"
    }

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ChatUp-notiser",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Allmänna notiser från ChatUp"
            }

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun showTestNotification() {
        ensureChannel()

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // بدّلها لو عندك أيقونة Notification خاصة
            .setContentTitle("ChatUp")
            .setContentText("Notiser är aktiverade ✅")
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notif)
    }
}