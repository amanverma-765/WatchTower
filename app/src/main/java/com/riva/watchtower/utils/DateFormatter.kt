package com.riva.watchtower.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateFormatter {
    private val shortFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    private val longFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())

    fun formatShort(timestamp: Long): String = shortFormat.format(Date(timestamp))
    fun formatLong(timestamp: Long): String = longFormat.format(Date(timestamp))

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
