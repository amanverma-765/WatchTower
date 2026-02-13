package com.riva.watchtower.domain.enums

enum class SiteStatus(val text: String) {
    PASSED("No Change"),
    CHANGED("Changed"),
    ERROR("Failed")
}
