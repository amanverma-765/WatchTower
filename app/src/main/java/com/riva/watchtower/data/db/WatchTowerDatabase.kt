package com.riva.watchtower.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SiteEntity::class], version = 1)
abstract class WatchTowerDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao
}
