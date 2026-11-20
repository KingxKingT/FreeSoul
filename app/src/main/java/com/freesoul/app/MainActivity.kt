package com.freesoul.app

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var statsText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val refresh = object : Runnable {
        override fun run() {
            updateStatus()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        handler.post(refresh)
    }

    override fun onPause() {
        handler.removeCallbacks(refresh)
        super.onPause()
    }

    @Suppress("DEPRECATION")
    private fun requestProtection() {
        val permissionIntent = VpnService.prepare(this)
        if (permissionIntent != null) startActivityForResult(permissionIntent, REQUEST_VPN)
        else startVpnService()
    }

    @Deprecated("Uses the VPN permission activity result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VPN && resultCode == RESULT_OK) startVpnService()
        updateStatus()
    }

    private fun startVpnService() {
        val intent = Intent(this, DnsVpnService::class.java).setAction(DnsVpnService.ACTION_START)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
        updateStatus()
    }

    private fun stopVpnService() {
        startService(Intent(this, DnsVpnService::class.java).setAction(DnsVpnService.ACTION_STOP))
        updateStatus()
    }

    private fun buildScreen() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(24))
            setBackgroundColor(0xFFF5F7F6.toInt())
        }
        val scroll = ScrollView(this).apply { addView(page) }

        page.addView(text("FreeSoul", 30f, true, 0xFF183B32.toInt()))
        page.addView(text("Private DNS protection", 16f, true, 0xFF236B57.toInt()).withTopMargin(4))
        page.addView(text("Test build 0.1.0 · Android 10 and newer", 13f, false, 0xFF5D6B66.toInt()).withTopMargin(8))

        statusText = text("Protection is off", 18f, true, 0xFF9B3D32.toInt()).withTopMargin(28)
        page.addView(statusText)
        page.addView(text("Start protection to let Android send DNS lookups through FreeSoul. The app checks the hostname locally and forwards allowed DNS requests to a public resolver.", 14f, false, 0xFF34423D.toInt()).withTopMargin(10))

        page.addView(Button(this).apply {
            text = "Start protection"
            setOnClickListener { requestProtection() }
        }, matchWrap().withMargins(top = 22))
        page.addView(Button(this).apply {
            text = "Stop protection"
            setOnClickListener { stopVpnService() }
        }, matchWrap().withMargins(top = 8))

        page.addView(text("Test blocklist", 18f, true, 0xFF183B32.toInt()).withTopMargin(28))
        page.addView(text("This test version contains 7 reviewed adult-purpose candidate domains. A rule also covers subdomains, for example sub.example.com when example.com is listed.", 14f, false, 0xFF34423D.toInt()).withTopMargin(8))
        page.addView(text("cake.ski\naipornfeed.com\nanimatorhub.ai\ncoquette.ai\nhoneypot.com\nplaybun.com\nsweetdream.ai", 14f, true, 0xFF183B32.toInt()).withTopMargin(10))

        page.addView(text("Activity", 18f, true, 0xFF183B32.toInt()).withTopMargin(28))
        statsText = text("Blocked DNS requests: 0\nLast blocked hostname: None yet", 14f, false, 0xFF34423D.toInt()).withTopMargin(8)
        page.addView(statsText)

        page.addView(text("Important test notes", 18f, true, 0xFF183B32.toInt()).withTopMargin(28))
        page.addView(text(
            "• This is an early DNS-only test, not a complete adult-content filter.\n" +
                "• It blocks hostnames, not individual pages or images.\n" +
                "• Browser Secure DNS / DNS-over-HTTPS or strict Private DNS may bypass this test. Turn those off temporarily when testing.\n" +
                "• Only one Android VPN can normally be active at a time.\n" +
                "• The test list is small; most adult sites are not included yet.\n" +
                "• Allowed DNS lookups are sent to Cloudflare or Google DNS. No browsing history is uploaded by FreeSoul.",
            13f, false, 0xFF5D6B66.toInt()
        ).withTopMargin(8))
        setContentView(scroll)
        updateStatus()
    }

    private fun updateStatus() {
        if (!::statusText.isInitialized || !::statsText.isInitialized) return
        val running = ProtectionState.running
        statusText.text = if (running) "Protection is active" else "Protection is off"
        statusText.setTextColor(if (running) 0xFF236B57.toInt() else 0xFF9B3D32.toInt())
        statsText.text = "Blocked DNS requests: ${ProtectionState.blockedCount.get()}\nLast blocked hostname: ${ProtectionState.lastBlockedDomain}"
    }

    private fun text(value: String, size: Float, bold: Boolean, color: Int): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setLineSpacing(dp(3).toFloat(), 1f)
    }

    private fun TextView.withTopMargin(margin: Int): TextView = apply {
        layoutParams = matchWrap().withMargins(top = margin)
    }

    private fun matchWrap() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun LinearLayout.LayoutParams.withMargins(top: Int = 0) = apply { topMargin = dp(top) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val REQUEST_VPN = 701
    }
}
