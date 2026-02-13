package com.riva.watchtower.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private val backgroundCheckEnabledKey = booleanPreferencesKey("background_check_enabled")
    private val checkIntervalMinutesKey = longPreferencesKey("check_interval_minutes")
    private val parallelPoolSizeKey = intPreferencesKey("parallel_pool_size")

    val isBackgroundCheckEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[backgroundCheckEnabledKey] ?: false }

    val checkIntervalMinutes: Flow<Long> = context.dataStore.data
        .map { it[checkIntervalMinutesKey] ?: 180L }

    val parallelPoolSize: Flow<Int> = context.dataStore.data
        .map { it[parallelPoolSizeKey] ?: 10 }

    suspend fun setBackgroundCheckEnabled(enabled: Boolean) {
        context.dataStore.edit { it[backgroundCheckEnabledKey] = enabled }
    }

    suspend fun setCheckIntervalMinutes(minutes: Long) {
        context.dataStore.edit { it[checkIntervalMinutesKey] = minutes }
    }

    suspend fun setParallelPoolSize(size: Int) {
        context.dataStore.edit { it[parallelPoolSizeKey] = size }
    }
}
