package com.riva.watchtower.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import com.riva.watchtower.data.local.SettingsDataStore
import com.riva.watchtower.domain.repository.SiteRepository
import com.riva.watchtower.utils.NotificationHelper
import com.riva.watchtower.utils.SiteCheckRunner
import kotlinx.coroutines.flow.first

class SiteCheckWorker(
    private val repository: SiteRepository,
    private val settingsDataStore: SettingsDataStore,
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val logger = Logger.withTag("SiteCheckWorker")

    override suspend fun doWork(): Result {
        val sites = repository.getAllSites()
        if (sites.isEmpty()) {
            logger.i { "No sites to check" }
            return Result.success()
        }

        val poolSize = settingsDataStore.parallelPoolSize.first()
        logger.i { "Starting background check for ${sites.size} sites (pool=$poolSize)" }

        try {
            setForeground(createForegroundInfo(0, sites.size, "Starting..."))
        } catch (e: Exception) {
            logger.w(e) { "Could not set foreground info" }
        }

        val stats = SiteCheckRunner.checkAll(
            sites = sites,
            poolSize = poolSize,
            repository = repository,
            onProgress = { progress ->
                try {
                    setForeground(
                        createForegroundInfo(progress.completed, progress.total, progress.siteName)
                    )
                } catch (e: Exception) {
                    logger.w(e) { "Could not update foreground info" }
                }
            }
        )

        NotificationHelper.sendResultNotification(applicationContext, stats)

        logger.i { "Background check complete: ${stats.changed} changed, ${stats.passed} passed, ${stats.error} errors" }
        return Result.success()
    }

    private fun createForegroundInfo(current: Int, total: Int, siteName: String): ForegroundInfo {
        val notification = NotificationHelper.buildProgressNotification(
            applicationContext, current, total, siteName
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                NotificationHelper.PROGRESS_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NotificationHelper.PROGRESS_NOTIFICATION_ID, notification)
        }
    }
}
