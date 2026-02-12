package com.riva.watchtower.domain.enums

enum class SiteStatus(val text: String) {
    CHANGED("Changed"),
    ERROR("Failed"),
    PASSED("No Change"),
    RESOLVED("Resolved")
}