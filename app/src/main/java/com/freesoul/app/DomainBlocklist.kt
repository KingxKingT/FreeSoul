package com.freesoul

import android.content.Context
import java.io.File
import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Lookup over adult_domains.bin: a sorted array of big-endian 64-bit FNV-1a
 * hashes (8 bytes per domain). Memory-mapped, binary search, ~0 heap use.
 * Put adult_domains.bin in app/src/main/assets/.
 */
class DomainBlocklist(context: Context) {

    private val buf: MappedByteBuffer
    private val count: Int

    init {
        val file = File(context.filesDir, "adult_domains.bin")
        // Copy from assets on first run (or when the app is updated).
        if (!file.exists() || file.length() == 0L) {
            context.assets.open("adult_domains.bin").use { input ->
                file.outputStream().use { input.copyTo(it) }
            }
        }
        val raf = RandomAccessFile(file, "r")
        buf = raf.channel.map(FileChannel.MapMode.READ_ONLY, 0, raf.length())
        count = (raf.length() / 8).toInt()
    }

    /** True if the host or any parent domain is on the list (label-boundary). */
    fun isBlocked(host: String): Boolean {
        var h = host.lowercase().trimEnd('.')
        while (true) {
            if (contains(fnv1a64(h))) return true
            val dot = h.indexOf('.')
            if (dot < 0 || h.indexOf('.', dot + 1) < 0) return false // keep at least a.b
            h = h.substring(dot + 1)
        }
    }

    private fun contains(key: Long): Boolean {
        var lo = 0
        var hi = count - 1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            val cmp = java.lang.Long.compareUnsigned(buf.getLong(mid * 8), key)
            when {
                cmp == 0 -> return true
                cmp < 0 -> lo = mid + 1
                else -> hi = mid - 1
            }
        }
        return false
    }

    private fun fnv1a64(s: String): Long {
        var h = -3750763034362895579L // 0xcbf29ce484222325
        for (b in s.toByteArray(Charsets.UTF_8)) {
            h = h xor (b.toLong() and 0xff)
            h *= 1099511628211L // 0x100000001b3
        }
        return h
    }
}
