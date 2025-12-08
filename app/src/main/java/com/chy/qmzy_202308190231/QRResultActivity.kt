package com.chy.qmzy_202308190231

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class QRResultActivity : AppCompatActivity() {

    private lateinit var tvFormat: TextView
    private lateinit var tvContent: TextView
    private lateinit var btnCopy: Button
    private lateinit var btnOpenLink: Button
    private lateinit var btnRescan: Button
    private lateinit var btnBack: Button
    
    private var scanResult: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_result)

        tvFormat = findViewById(R.id.tvFormat)
        tvContent = findViewById(R.id.tvContent)
        btnCopy = findViewById(R.id.btnCopy)
        btnOpenLink = findViewById(R.id.btnOpenLink)
        btnRescan = findViewById(R.id.btnRescan)
        btnBack = findViewById(R.id.btnBack)

        scanResult = intent.getStringExtra("SCAN_RESULT") ?: ""
        val format = intent.getStringExtra("FORMAT") ?: "未知"

        tvFormat.text = format
        tvContent.text = scanResult

        if (isUrl(scanResult)) {
            btnOpenLink.visibility = View.VISIBLE
        } else {
            btnOpenLink.visibility = View.GONE
        }

        btnCopy.setOnClickListener {
            copyToClipboard(scanResult)
        }

        btnOpenLink.setOnClickListener {
            openUrl(scanResult)
        }

        btnRescan.setOnClickListener {
            val intent = Intent(this, QRScanActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun isUrl(text: String): Boolean {
        return text.startsWith("http://", ignoreCase = true) ||
                text.startsWith("https://", ignoreCase = true) ||
                text.startsWith("www.", ignoreCase = true)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("二维码内容", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }

    private fun openUrl(url: String) {
        try {
            var finalUrl = url
            if (!url.startsWith("http://", ignoreCase = true) && 
                !url.startsWith("https://", ignoreCase = true)) {
                finalUrl = "https://$url"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开链接: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
