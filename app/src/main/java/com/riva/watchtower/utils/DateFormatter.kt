package com.riva.watchtower.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateFormatter {
    fun formatShort(timestamp: Long): String =
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))

    fun formatLong(timestamp: Long): String =
        SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(Date(timestamp))

    fun formatCountdown(targetMillis: Long): String {
        val diff = targetMillis - System.currentTimeMillis()
        if (diff <= 0) return "soon"
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
}
