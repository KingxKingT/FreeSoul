package com.freesoul.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

class DnsVpnService : VpnService() {
    private var tun: ParcelFileDescriptor? = null
    private var worker: Thread? = null
    private val active = AtomicBoolean(false)
    private lateinit var customDomains: Set<String>
    private lateinit var blocklist: DomainBlocklist

    override fun onCreate() {
        super.onCreate()
        blocklist = DomainBlocklist(applicationContext)
        customDomains = assets.open("adult_domains.txt").bufferedReader().useLines { lines ->
            lines.mapNotNull { DomainMatcher.normalize(it.substringBefore('#').trim()) }.toSet()
        }
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopFiltering()
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, buildNotification())
        if (!active.get()) startFiltering()
        return START_STICKY
    }

    private fun startFiltering() {
        try {
            val descriptor = Builder()
                .setSession("FreeSoul DNS protection")
                .setMtu(1500)
                .addAddress(VPN_ADDRESS, 32)
                .addDnsServer(DNS_ADDRESS)
                .addRoute(DNS_ADDRESS, 32)
                .establish()

            if (descriptor == null) {
                ProtectionState.running = false
                stopSelf()
                return
            }
            tun = descriptor
            active.set(true)
            ProtectionState.running = true
            worker = Thread({ packetLoop(descriptor) }, "FreeSoulDnsLoop").apply { start() }
        } catch (_: Exception) {
            ProtectionState.running = false
            stopFiltering()
            stopSelf()
        }
    }

    private fun packetLoop(descriptor: ParcelFileDescriptor) {
        val input = FileInputStream(descriptor.fileDescriptor)
        val output = FileOutputStream(descriptor.fileDescriptor)
        val buffer = ByteArray(32767)
        try {
            while (active.get() && !Thread.currentThread().isInterrupted) {
                val length = input.read(buffer)
                if (length <= 0) continue
                val query = DnsPacketCodec.parseIpv4UdpQuery(buffer, length) ?: continue
                val host = DomainMatcher.normalize(query.hostname)
                val blocked = host != null &&
                    (blocklist.isBlocked(host) || DomainMatcher.isBlocked(host, customDomains))
                val replyDns = if (blocked) {
                    ProtectionState.onBlocked(query.hostname)
                    DnsPacketCodec.makeDnsError(query, 3)
                } else {
                    forwardToUpstream(query.dnsMessage) ?: DnsPacketCodec.makeDnsError(query, 2)
                }
                output.write(DnsPacketCodec.wrapDnsResponse(query, replyDns))
                output.flush()
            }
        } catch (_: Exception) {
            // Closing the TUN descriptor during stop/revoke is expected.
        } finally {
            ProtectionState.running = false
            active.set(false)
            try { input.close() } catch (_: Exception) { }
            try { output.close() } catch (_: Exception) { }
            stopSelf()
        }
    }

    private fun forwardToUpstream(message: ByteArray): ByteArray? {
        for (upstream in listOf("1.1.1.1", "8.8.8.8")) {
            try {
                DatagramSocket().use { socket ->
                    if (!protect(socket)) return null
                    socket.soTimeout = DNS_TIMEOUT_MS
                    val address = InetAddress.getByName(upstream)
                    socket.send(DatagramPacket(message, message.size, address, 53))
                    val responseBuffer = ByteArray(4096)
                    val response = DatagramPacket(responseBuffer, responseBuffer.size)
                    socket.receive(response)
                    if (response.length >= 12) return response.data.copyOf(response.length)
                }
            } catch (_: Exception) {
                // Try the next resolver.
            }
        }
        return null
    }

    private fun stopFiltering() {
        active.set(false)
        ProtectionState.running = false
        try { tun?.close() } catch (_: Exception) { }
        tun = null
        worker?.interrupt()
        worker = null
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onRevoke() {
        stopFiltering()
        stopSelf()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopFiltering()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "FreeSoul protection", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Shows when local DNS protection is active" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder.setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("FreeSoul protection is active")
            .setContentText("Filtering DNS requests locally. Tap to open FreeSoul.")
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START = "com.freesoul.app.START"
        const val ACTION_STOP = "com.freesoul.app.STOP"
        private const val CHANNEL_ID = "freesoul_protection"
        private const val NOTIFICATION_ID = 2401
        private const val VPN_ADDRESS = "10.111.0.1"
        private const val DNS_ADDRESS = "10.111.0.2"
        private const val DNS_TIMEOUT_MS = 2500
    }
}
