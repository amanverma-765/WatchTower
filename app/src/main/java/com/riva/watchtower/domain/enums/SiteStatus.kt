package com.riva.watchtower.domain.enums

enum class SiteStatus(val text: String) {
    RESOLVED("Resolved"),
    PASSED("No Change"),
    CHANGED("Changed"),
    ERROR("Failed")
}