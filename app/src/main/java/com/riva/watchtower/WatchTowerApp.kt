package com.riva.watchtower

import android.app.Application
import androidx.work.Configuration
import com.riva.watchtower.di.appModule
import com.riva.watchtower.di.workerModule
import com.riva.watchtower.utils.NotificationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext.startKoin

class WatchTowerApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@WatchTowerApp)
            workManagerFactory()
            modules(appModule, workerModule)
        }

        NotificationHelper.createChannels(this)
    }
}