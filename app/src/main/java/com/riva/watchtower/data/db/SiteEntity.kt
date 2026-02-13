package com.riva.watchtower.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.riva.watchtower.domain.enums.SiteStatus
import com.riva.watchtower.domain.models.Site

@Entity(tableName = "sites")
data class SiteEntity(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val favicon: String,
    val lastCheckedAt: Long,
    val createdAt: Long,
    val lastStatus: String,
    val contentHash: String
)

fun SiteEntity.toDomain(): Site = Site(
    id = id,
    name = name,
    url = url,
    favicon = favicon,
    lastCheckedAt = lastCheckedAt,
    createdAt = createdAt,
    lastStatus = SiteStatus.valueOf(lastStatus),
    contentHash = contentHash
)

fun Site.toEntity(): SiteEntity = SiteEntity(
    id = id,
    name = name,
    url = url,
    favicon = favicon,
    lastCheckedAt = lastCheckedAt,
    createdAt = createdAt,
    lastStatus = lastStatus.name,
    contentHash = contentHash
)
