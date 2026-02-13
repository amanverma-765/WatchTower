package com.riva.watchtower

import android.app.Application
import com.riva.watchtower.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class WatchTowerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@WatchTowerApp)
            modules(appModule)
        }
    }
}
