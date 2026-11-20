package com.freesoul.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DnsPacketCodecTest {
    @Test fun parsesIpv4UdpDnsQuery() {
        val packet = makeQueryPacket("sub.cake.ski")
        val query = DnsPacketCodec.parseIpv4UdpQuery(packet, packet.size)
        assertNotNull(query)
        assertEquals("sub.cake.ski", query!!.hostname)
    }

    @Test fun rejectsNonDnsDestinationPort() {
        val packet = makeQueryPacket("cake.ski")
        packet[22] = 0x01
        packet[23] = 0xbb.toByte()
        assertNull(DnsPacketCodec.parseIpv4UdpQuery(packet, packet.size))
    }

    @Test fun createsNxdomainAndValidIpv4UdpReply() {
        val packet = makeQueryPacket("cake.ski")
        val query = DnsPacketCodec.parseIpv4UdpQuery(packet, packet.size)!!
        val dnsReply = DnsPacketCodec.makeDnsError(query, 3)
        assertEquals(3, dnsReply[3].toInt() and 0x0f)
        assertEquals(1, dnsReply[5].toInt() and 0xff)

        val reply = DnsPacketCodec.wrapDnsResponse(query, dnsReply)
        assertEquals(0x45, reply[0].toInt() and 0xff)
        assertEquals(17, reply[9].toInt() and 0xff)
        assertEquals(53, ((reply[20].toInt() and 0xff) shl 8) or (reply[21].toInt() and 0xff))
        assertTrue(reply.size >= 20 + 8 + 12)
        assertEquals(0xffff, ipv4ChecksumSum(reply, 20))
    }

    private fun makeQueryPacket(hostname: String): ByteArray {
        val dns = ArrayList<Byte>()
        repeat(2) { dns.add(0) }
        dns.add(0x01); dns.add(0x00)
        dns.add(0x00); dns.add(0x01)
        repeat(6) { dns.add(0) }
        hostname.split('.').forEach { label ->
            val bytes = label.toByteArray(Charsets.US_ASCII)
            dns.add(bytes.size.toByte())
            bytes.forEach { dns.add(it) }
        }
        dns.add(0)
        dns.add(0); dns.add(1)
        dns.add(0); dns.add(1)
        val dnsBytes = dns.toByteArray()
        val udpLength = 8 + dnsBytes.size
        val totalLength = 20 + udpLength
        val packet = ByteArray(totalLength)
        val b = ByteBuffer.wrap(packet).order(ByteOrder.BIG_ENDIAN)
        b.put(0, 0x45.toByte())
        b.putShort(2, totalLength.toShort())
        b.put(8, 64.toByte())
        b.put(9, 17.toByte())
        packet[12] = 10; packet[13] = 0; packet[14] = 0; packet[15] = 10
        packet[16] = 10; packet[17] = 111; packet[18] = 0; packet[19] = 2
        b.putShort(20, 53000.toShort())
        b.putShort(22, 53.toShort())
        b.putShort(24, udpLength.toShort())
        b.putShort(26, 0)
        System.arraycopy(dnsBytes, 0, packet, 28, dnsBytes.size)
        return packet
    }

    private fun ipv4ChecksumSum(packet: ByteArray, length: Int): Int {
        var sum = 0
        var i = 0
        while (i < length) {
            sum += ((packet[i].toInt() and 0xff) shl 8) or (packet[i + 1].toInt() and 0xff)
            i += 2
        }
        while ((sum ushr 16) != 0) sum = (sum and 0xffff) + (sum ushr 16)
        return sum
    }
}
