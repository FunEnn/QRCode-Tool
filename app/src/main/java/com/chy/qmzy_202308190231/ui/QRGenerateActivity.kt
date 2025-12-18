package com.chy.qmzy_202308190231.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.chy.qmzy_202308190231.utils.ImageShareUtils
import com.chy.qmzy_202308190231.utils.ImageStoreUtils
import com.chy.qmzy_202308190231.R
import com.chy.qmzy_202308190231.viewmodel.GenerateViewModel

class QRGenerateActivity : AppCompatActivity() {

    // 类型切换按钮
    private lateinit var btnTypeText: Button
    private lateinit var btnTypeUrl: Button

    private lateinit var etContent: EditText
    private lateinit var btnGenerate: Button

    // 结果展示区域
    private lateinit var cardResult: View // 结果卡片容器
    private lateinit var tvQRLabel: TextView
    private lateinit var ivQRCode: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnShare: Button
    private var btnBack: ImageButton? = null
    private lateinit var header: MaterialToolbar

    private var currentQRBitmap: Bitmap? = null

    private val viewModel: GenerateViewModel by viewModels()

    companion object {
        private const val STORAGE_PERMISSION_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_generate)

        initViews()
        setupListeners()

        // 初始化为文本类型
        switchType(GenerateViewModel.QrType.TEXT)

        viewModel.uiState.observe(this) { state ->
            // 同步 bitmap 到 UI
            currentQRBitmap = state.bitmap
            if (state.bitmap != null) {
                ivQRCode.setImageBitmap(state.bitmap)
                cardResult.visibility = View.VISIBLE
            } else {
                cardResult.visibility = View.GONE
            }
        }

        viewModel.event.observe(this) { wrapper ->
            val event = wrapper?.getContentIfNotHandled() ?: return@observe
            when (event) {
                is GenerateViewModel.Event.Toast -> {
                    Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                }
                is GenerateViewModel.Event.UpdateInput -> {
                    etContent.setText(event.text)
                }
                is GenerateViewModel.Event.RequestSave -> {
                    handleSave(event.bitmap)
                }
                is GenerateViewModel.Event.RequestShare -> {
                    ImageShareUtils.shareBitmapPng(this, event.bitmap, "share_qr.png")
                }
            }
        }
    }

    private fun initViews() {
        header = findViewById(R.id.header)

        // 初始化类型切换按钮
        btnTypeText = findViewById(R.id.btnTypeText)
        btnTypeUrl = findViewById(R.id.btnTypeUrl)

        etContent = findViewById(R.id.etContent)
        btnGenerate = findViewById(R.id.btnGenerate)

        // 结果区域
        cardResult = findViewById(R.id.cardResult) // 绑定卡片容器
        tvQRLabel = findViewById(R.id.tvQRLabel)
        ivQRCode = findViewById(R.id.ivQRCode)
        btnSave = findViewById(R.id.btnSave)
        btnShare = findViewById(R.id.btnShare)
        btnBack = findViewById<ImageButton?>(R.id.btnBack)
    }

    private fun setupListeners() {
        // 类型切换监听
        btnTypeText.setOnClickListener { switchType(GenerateViewModel.QrType.TEXT) }
        btnTypeUrl.setOnClickListener { switchType(GenerateViewModel.QrType.URL) }

        btnGenerate.setOnClickListener {
            viewModel.onGenerateClicked(etContent.text?.toString() ?: "")
        }

        btnSave.setOnClickListener {
            viewModel.onSaveClicked()
        }

        btnShare.setOnClickListener {
            viewModel.onShareClicked()
        }

        btnBack?.setOnClickListener { finish() }
        header.setNavigationOnClickListener { finish() }
    }

    private fun switchType(type: GenerateViewModel.QrType) {
        viewModel.setType(type)

        // 切换类型时，隐藏之前的生成结果，避免混淆
        if (::cardResult.isInitialized) {
            cardResult.visibility = View.GONE
        }

        // 更新按钮颜色
        val selectedColor = getColor(android.R.color.holo_blue_dark)
        val unselectedColor = getColor(android.R.color.darker_gray)

        btnTypeText.setBackgroundColor(if (type == GenerateViewModel.QrType.TEXT) selectedColor else unselectedColor)
        btnTypeUrl.setBackgroundColor(if (type == GenerateViewModel.QrType.URL) selectedColor else unselectedColor)

        // 按钮文字颜色反转逻辑（可选，为了更好的视觉效果）
        btnTypeText.setTextColor(if (type == GenerateViewModel.QrType.TEXT) Color.WHITE else Color.parseColor("#666666"))
        btnTypeUrl.setTextColor(if (type == GenerateViewModel.QrType.URL) Color.WHITE else Color.parseColor("#666666"))

        // 更新界面显示
        when (type) {
            GenerateViewModel.QrType.TEXT -> {
                etContent.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            }
            GenerateViewModel.QrType.URL -> {
                etContent.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            }
        }
    }

    private fun handleSave(bitmap: Bitmap) {
        // Android 10 (API 29) 及以上通常不需要写权限即可保存到相册
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveBitmapToGallery(bitmap)
        } else {
            if (checkStoragePermission()) {
                saveBitmapToGallery(bitmap)
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveQRCodeToGallery()
            } else {
                Toast.makeText(this, "需要存储权限才能保存图片", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveQRCodeToGallery() {
        val bitmap = currentQRBitmap ?: return
        saveBitmapToGallery(bitmap)
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "QR_${System.currentTimeMillis()}.jpg"
        try {
            val uri = ImageStoreUtils.saveJpegToGallery(this, bitmap, filename)
            if (uri != null) {
                Toast.makeText(this, "已保存到相册", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}