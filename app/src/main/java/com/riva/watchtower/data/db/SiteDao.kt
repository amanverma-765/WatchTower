package com.riva.watchtower.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {
    @Query("SELECT * FROM sites ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SiteEntity>>

    @Query("SELECT * FROM sites")
    suspend fun getAll(): List<SiteEntity>

    @Query("SELECT * FROM sites WHERE id = :id")
    suspend fun getById(id: String): SiteEntity?

    @Upsert
    suspend fun upsert(site: SiteEntity)

    @Query("DELETE FROM sites WHERE id = :id")
    suspend fun deleteById(id: String)
}
