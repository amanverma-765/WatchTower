package com.riva.watchtower.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.riva.watchtower.MainActivity
import com.riva.watchtower.R

data class CheckStats(
    val total: Int,
    val changed: Int,
    val passed: Int,
    val error: Int
)

object NotificationHelper {

    const val PROGRESS_CHANNEL_ID = "site_check_progress"
    const val CHANGES_CHANNEL_ID = "site_changes"
    const val PROGRESS_NOTIFICATION_ID = 1001
    private const val CHANGES_NOTIFICATION_ID = 1002

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val progressChannel = NotificationChannel(
            PROGRESS_CHANNEL_ID,
            "Site Check Progress",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows progress while checking sites in the background"
            setSound(null, null)
        }

        val changesChannel = NotificationChannel(
            CHANGES_CHANNEL_ID,
            "Site Changes",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when monitored sites have changed"
        }

        manager.createNotificationChannels(listOf(progressChannel, changesChannel))
    }

    fun buildProgressNotification(
        context: Context,
        current: Int,
        total: Int,
        siteName: String
    ): android.app.Notification {
        val friendly = UrlUtils.friendlyName(siteName)
        return NotificationCompat.Builder(context, PROGRESS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Checking sites")
            .setContentText("Checking $friendly ($current/$total)")
            .setProgress(total, current, false)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun sendResultNotification(context: Context, stats: CheckStats) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = when {
            stats.changed == 0 -> "All sites up to date"
            else -> "${stats.changed} out of ${stats.total} sites have changed"
        }

        val statsLine = "${stats.changed} changed, ${stats.passed} passed, ${stats.error} failed"

        val notification = NotificationCompat.Builder(context, CHANGES_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(statsLine)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(CHANGES_NOTIFICATION_ID, notification)
    }
}
