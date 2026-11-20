package com.freesoul.app

import java.util.concurrent.atomic.AtomicInteger

object ProtectionState {
    @Volatile var running: Boolean = false
    @Volatile var lastBlockedDomain: String = "None yet"
    val blockedCount = AtomicInteger(0)

    fun onBlocked(domain: String) {
        lastBlockedDomain = domain
        blockedCount.incrementAndGet()
    }
}
