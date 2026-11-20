package com.freesoul.app

import java.nio.ByteBuffer
import java.nio.ByteOrder

internal data class DnsQuery(
    val sourceAddress: ByteArray,
    val destinationAddress: ByteArray,
    val sourcePort: Int,
    val destinationPort: Int,
    val dnsMessage: ByteArray,
    val questionEnd: Int,
    val hostname: String
)

/** Minimal IPv4/UDP DNS codec for the local DNS-only VPN interface. */
internal object DnsPacketCodec {
    fun parseIpv4UdpQuery(packet: ByteArray, length: Int): DnsQuery? {
        if (length < 20) return null
        val version = (packet[0].toInt() ushr 4) and 0x0f
        if (version != 4) return null
        val ihl = (packet[0].toInt() and 0x0f) * 4
        if (ihl < 20 || length < ihl + 8) return null
        val totalLength = u16(packet, 2)
        if (totalLength > length || totalLength < ihl + 8) return null
        if ((packet[9].toInt() and 0xff) != 17) return null
        if ((u16(packet, 6) and 0x1fff) != 0) return null

        val udpOffset = ihl
        val sourcePort = u16(packet, udpOffset)
        val destinationPort = u16(packet, udpOffset + 2)
        if (destinationPort != 53) return null
        val udpLength = u16(packet, udpOffset + 4)
        if (udpLength < 8 || udpOffset + udpLength > totalLength) return null
        val dnsStart = udpOffset + 8
        val dns = packet.copyOfRange(dnsStart, udpOffset + udpLength)
        if (dns.size < 17 || (u16(dns, 2) and 0x8000) != 0 || u16(dns, 4) != 1) return null

        val parsedName = parseQuestionName(dns) ?: return null
        val (hostname, questionEnd) = parsedName
        if (questionEnd > dns.size || questionEnd < 17) return null
        return DnsQuery(
            sourceAddress = packet.copyOfRange(12, 16),
            destinationAddress = packet.copyOfRange(16, 20),
            sourcePort = sourcePort,
            destinationPort = destinationPort,
            dnsMessage = dns,
            questionEnd = questionEnd,
            hostname = hostname
        )
    }

    fun makeDnsError(query: DnsQuery, rcode: Int): ByteArray {
        val response = query.dnsMessage.copyOfRange(0, query.questionEnd)
        val originalFlags = u16(query.dnsMessage, 2)
        val responseFlags = 0x8000 or (originalFlags and 0x7800) or
            (originalFlags and 0x0100) or 0x0080 or (rcode and 0x000f)
        putU16(response, 2, responseFlags)
        putU16(response, 4, 1)
        putU16(response, 6, 0)
        putU16(response, 8, 0)
        putU16(response, 10, 0)
        return response
    }

    fun wrapDnsResponse(query: DnsQuery, dnsResponse: ByteArray): ByteArray {
        val udpLength = 8 + dnsResponse.size
        val totalLength = 20 + udpLength
        require(totalLength <= 65535)
        val result = ByteArray(totalLength)
        val buffer = ByteBuffer.wrap(result).order(ByteOrder.BIG_ENDIAN)
        buffer.put(0, 0x45.toByte())
        buffer.put(1, 0)
        buffer.putShort(2, totalLength.toShort())
        buffer.putShort(4, 0)
        buffer.putShort(6, 0x4000.toShort())
        buffer.put(8, 64)
        buffer.put(9, 17)
        buffer.putShort(10, 0)
        System.arraycopy(query.destinationAddress, 0, result, 12, 4)
        System.arraycopy(query.sourceAddress, 0, result, 16, 4)
        putU16(result, 10, checksum(result, 0, 20))

        putU16(result, 20, query.destinationPort)
        putU16(result, 22, query.sourcePort)
        putU16(result, 24, udpLength)
        putU16(result, 26, 0)
        System.arraycopy(dnsResponse, 0, result, 28, dnsResponse.size)
        return result
    }

    private fun parseQuestionName(dns: ByteArray): Pair<String, Int>? {
        var cursor = 12
        val labels = ArrayList<String>()
        while (cursor < dns.size) {
            val size = dns[cursor].toInt() and 0xff
            cursor++
            if (size == 0) break
            if ((size and 0xc0) != 0 || size > 63 || cursor + size > dns.size) return null
            val labelBytes = dns.copyOfRange(cursor, cursor + size)
            if (labelBytes.any { (it.toInt() and 0xff) !in 33..126 }) return null
            labels.add(String(labelBytes, Charsets.US_ASCII))
            cursor += size
        }
        if (labels.isEmpty() || cursor + 4 > dns.size) return null
        return labels.joinToString(".").lowercase() to (cursor + 4)
    }

    private fun checksum(data: ByteArray, offset: Int, length: Int): Int {
        var sum = 0L
        var i = offset
        val end = offset + length
        while (i + 1 < end) {
            sum += (((data[i].toInt() and 0xff) shl 8) or (data[i + 1].toInt() and 0xff)).toLong()
            i += 2
        }
        if (i < end) sum += ((data[i].toInt() and 0xff) shl 8).toLong()
        while ((sum ushr 16) != 0L) sum = (sum and 0xffff) + (sum ushr 16)
        return sum.inv().toInt() and 0xffff
    }

    private fun u16(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xff) shl 8) or (bytes[offset + 1].toInt() and 0xff)

    private fun putU16(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = ((value ushr 8) and 0xff).toByte()
        bytes[offset + 1] = (value and 0xff).toByte()
    }
}
