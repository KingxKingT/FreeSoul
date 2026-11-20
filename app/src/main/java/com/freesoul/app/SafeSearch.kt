package com.freesoul.app

import java.io.ByteArrayOutputStream

/** Forces SafeSearch by answering search-engine DNS lookups with their "safe" servers. */
internal object SafeSearch {

    // Google has one domain per country: google.com, google.tn, google.co.uk, google.com.eg ...
    private val googleRegex =
        Regex("^(www\\.)?google\\.(com|[a-z]{2}|co\\.[a-z]{2}|com\\.[a-z]{2})$")

    private val exact = mapOf(
        "bing.com" to "strict.bing.com",
        "www.bing.com" to "strict.bing.com",
        "duckduckgo.com" to "safe.duckduckgo.com",
        "www.duckduckgo.com" to "safe.duckduckgo.com",
        "www.youtube.com" to "restrict.youtube.com",
        "m.youtube.com" to "restrict.youtube.com",
        "youtubei.googleapis.com" to "restrict.youtube.com",
        "youtube.googleapis.com" to "restrict.youtube.com",
        "www.youtube-nocookie.com" to "restrict.youtube.com",
        "pixabay.com" to "safesearch.pixabay.com",
        "search.brave.com" to "forcesafe.search.brave.com",
    )

    // Browser DNS-over-HTTPS providers (they would bypass this app) + search engines without SafeSearch lock.
    private val bypassHosts = setOf(
        "dns.google", "dns.google.com", "cloudflare-dns.com", "chrome.cloudflare-dns.com",
        "mozilla.cloudflare-dns.com", "one.one.one.one", "dns.quad9.net", "doh.opendns.com",
        "dns.nextdns.io", "doh.cleanbrowsing.org", "dns.adguard-dns.com",
        "use-application-dns.net",
        "yandex.com", "yandex.ru", "yandex.tr", "yandex.by", "yandex.kz",
    )

    fun targetFor(host: String): String? {
        val h = host.lowercase().trimEnd('.')
        if (googleRegex.matches(h)) return "forcesafesearch.google.com"
        return exact[h]
    }

    fun isBypassHost(host: String): Boolean {
        var h = host.lowercase().trimEnd('.')
        while (true) {
            if (h in bypassHosts) return true
            val dot = h.indexOf('.')
            if (dot < 0) return false
            h = h.substring(dot + 1)
        }
    }
}

/** Small DNS helpers (A-record query, parse answers, build an A reply) on top of DnsPacketCodec. */
internal object SafeSearchDns {

    fun buildAQuery(host: String): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(0x46); out.write(0x53)             // id
        out.write(0x01); out.write(0x00)             // RD
        out.write(0); out.write(1)                   // 1 question
        repeat(6) { out.write(0) }
        for (label in host.trimEnd('.').split('.')) {
            out.write(label.length)
            out.write(label.toByteArray(Charsets.US_ASCII))
        }
        out.write(0)
        out.write(0); out.write(1)                   // type A
        out.write(0); out.write(1)                   // class IN
        return out.toByteArray()
    }

    fun parseARecords(r: ByteArray): List<ByteArray> {
        if (r.size < 12) return emptyList()
        if ((r[3].toInt() and 0x0f) != 0) return emptyList()
        val qd = u16(r, 4)
        val an = u16(r, 6)
        var i = 12
        fun skipName() {
            while (i < r.size) {
                val l = r[i].toInt() and 0xff
                if (l == 0) { i++; return }
                if ((l and 0xc0) == 0xc0) { i += 2; return }
                i += l + 1
            }
        }
        repeat(qd) { skipName(); i += 4 }
        val ips = ArrayList<ByteArray>()
        for (n in 0 until an) {
            if (i >= r.size) break
            skipName()
            if (i + 10 > r.size) break
            val type = u16(r, i)
            val rdlen = u16(r, i + 8)
            i += 10
            if (i + rdlen > r.size) break
            if (type == 1 && rdlen == 4 && ips.size < 8) ips.add(r.copyOfRange(i, i + 4))
            i += rdlen
        }
        return ips
    }

    /** Reply to an A query with [ips] (header + question come from DnsPacketCodec). */
    fun makeAResponse(query: DnsQuery, ips: List<ByteArray>): ByteArray {
        val base = DnsPacketCodec.makeDnsError(query, 0)   // NOERROR, no answers yet
        val out = ByteArrayOutputStream()
        out.write(base)
        for (ip in ips) {
            out.write(0xC0); out.write(0x0C)               // name = question
            out.write(0); out.write(1)                     // A
            out.write(0); out.write(1)                     // IN
            out.write(0); out.write(0); out.write(0); out.write(60)   // TTL 60s
            out.write(0); out.write(4)
            out.write(ip)
        }
        val bytes = out.toByteArray()
        bytes[6] = 0
        bytes[7] = ips.size.toByte()                       // answer count
        return bytes
    }

    private fun u16(b: ByteArray, o: Int): Int =
        ((b[o].toInt() and 0xff) shl 8) or (b[o + 1].toInt() and 0xff)
}
