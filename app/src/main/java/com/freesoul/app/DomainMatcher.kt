package com.freesoul.app

import java.util.Locale

/** Matches a domain rule against that exact hostname and its DNS subdomains only. */
object DomainMatcher {
    fun isBlocked(hostname: String, blockedDomains: Set<String>): Boolean {
        val host = normalize(hostname) ?: return false
        return blockedDomains.any { rawRule ->
            val rule = normalize(rawRule) ?: return@any false
            host == rule || host.endsWith(".$rule")
        }
    }

    fun normalize(value: String): String? {
        val host = value.trim().trimEnd('.').lowercase(Locale.ROOT)
        if (host.isEmpty() || host.length > 253 || host.contains('/') || host.contains(':')) return null
        val labels = host.split('.')
        if (labels.size < 2) return null
        if (labels.any { label ->
                label.isEmpty() || label.length > 63 ||
                    !label.first().isLetterOrDigit() || !label.last().isLetterOrDigit() ||
                    label.any { !it.isLetterOrDigit() && it != '-' }
            }) return null
        return host
    }
}
