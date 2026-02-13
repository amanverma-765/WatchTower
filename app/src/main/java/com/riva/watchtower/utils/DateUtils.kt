package com.riva.watchtower.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val shortFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    private val longFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())

    fun formatShort(timestamp: Long): String = shortFormat.format(Date(timestamp))
    fun formatLong(timestamp: Long): String = longFormat.format(Date(timestamp))
}
