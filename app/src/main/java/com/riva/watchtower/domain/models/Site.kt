package com.riva.watchtower.domain.models

import com.riva.watchtower.domain.enums.SiteStatus

data class Site(
    val id: String,
    val name: String,
    val url: String,
    val favicon: String,
    val lastCheckedAt: Long,
    val createdAt: Long,
    val lastStatus: SiteStatus,
    val contentHash: String
)
