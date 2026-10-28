package com.webapk.generated

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast

class MainActivity : Activity() {
    private val websiteUrl = "https://example.com"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this)
        val web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = WebViewClient()
        web.webChromeClient = WebChromeClient()
        web.loadUrl(websiteUrl)
        root.addView(web, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
        if (Settings.canDrawOverlays(this)) {
            startService(Intent(this, OverlayService::class.java))
        } else {
            Toast.makeText(this, "Aktifkan izin tampil di atas aplikasi lain untuk memakai tombol mengambang", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
    }
    override fun onDestroy() {
        (findViewById<FrameLayout>(android.R.id.content))?.let { root ->
            for (i in 0 until root.childCount) (root.getChildAt(i) as? WebView)?.destroy()
        }
        super.onDestroy()
    }
}
