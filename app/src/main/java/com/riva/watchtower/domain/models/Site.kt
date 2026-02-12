package com.riva.watchtower.domain.models

import com.riva.watchtower.domain.enums.SiteStatus

data class Site(
    val id: String,
    val name: String,
    val url: String,
    val favicon: String,
    val lastUpdated: Boolean,
    val lastStatus: SiteStatus
)