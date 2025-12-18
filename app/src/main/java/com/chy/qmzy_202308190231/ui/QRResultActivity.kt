package com.chy.qmzy_202308190231.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.chy.qmzy_202308190231.R
import com.chy.qmzy_202308190231.navigateBack
import com.chy.qmzy_202308190231.navigateToScan
import com.chy.qmzy_202308190231.viewmodel.ResultViewModel
import com.google.android.material.appbar.MaterialToolbar

class QRResultActivity : AppCompatActivity() {

    private lateinit var tvFormat: TextView
    private lateinit var tvContent: TextView
    private lateinit var btnCopy: Button
    private lateinit var btnOpenLink: Button
    private lateinit var btnRescan: Button
    private var btnBack: ImageButton? = null
    private lateinit var header: MaterialToolbar

    private val viewModel: ResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_result)

        header = findViewById(R.id.header)

        tvFormat = findViewById(R.id.tvFormat)
        tvContent = findViewById(R.id.tvContent)
        btnCopy = findViewById(R.id.btnCopy)
        btnOpenLink = findViewById(R.id.btnOpenLink)
        btnRescan = findViewById(R.id.btnRescan)
        btnBack = findViewById<ImageButton?>(R.id.btnBack)

        val scanResult = intent.getStringExtra("SCAN_RESULT") ?: ""
        val format = intent.getStringExtra("FORMAT") ?: "未知"
        viewModel.init(format, scanResult)

        viewModel.uiState.observe(this) { state ->
            tvFormat.text = state.format
            tvContent.text = state.content
            btnOpenLink.text = state.openActionText
            btnOpenLink.visibility = if (state.showOpenLink) View.VISIBLE else View.GONE
        }

        viewModel.event.observe(this) { wrapper ->
            val event = wrapper?.getContentIfNotHandled() ?: return@observe
            when (event) {
                is ResultViewModel.Event.Copy -> copyToClipboard(event.text)
                is ResultViewModel.Event.OpenLink -> openUri(event.url)
            }
        }

        btnCopy.setOnClickListener {
            viewModel.onCopyClicked()
        }

        btnOpenLink.setOnClickListener {
            viewModel.onOpenLinkClicked()
        }

        btnRescan.setOnClickListener {
            navigateToScan()
        }

        btnBack?.setOnClickListener { navigateBack() }
        header.setNavigationOnClickListener { navigateBack() }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("二维码内容", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }

    private fun openUri(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开链接: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}