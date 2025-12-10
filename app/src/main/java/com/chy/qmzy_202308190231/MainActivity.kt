package com.chy.qmzy_202308190231

import android.content.Intent
import android.os.Bundle
import android.view.View // 引入 View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var btnGenerate: View
    private lateinit var btnStartScan: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. 获取生成二维码按钮 (注意 ID 变成了 btnGenerateCard)
        btnGenerate = findViewById(R.id.btnGenerateCard)

        // 2. 获取扫一扫按钮 (注意 ID 是 btnStartScanCard)
        btnStartScan = findViewById(R.id.btnStartScanCard)

        // 3. 设置生成按钮点击事件
        btnGenerate.setOnClickListener {
            val intent = Intent(this, QRGenerateActivity::class.java)
            startActivity(intent)
        }

        // 4. 设置扫一扫按钮点击事件
        btnStartScan.setOnClickListener {
            val intent = Intent(this, QRScanActivity::class.java)
            startActivity(intent)
        }

        val btnDesignCard: View = findViewById(R.id.btnDesignCard)
        btnDesignCard.setOnClickListener {
            startActivity(Intent(this, QRDesignActivity::class.java))
        }

    }
}
