package com.chy.qmzy_202308190231

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

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
    private lateinit var btnBack: ImageButton

    private var currentQRBitmap: Bitmap? = null
    private var currentType: QRType = QRType.TEXT

    enum class QRType {
        TEXT, URL
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 102
        private const val QR_CODE_SIZE = 800
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_generate)

        initViews()
        setupListeners()

        // 初始化为文本类型
        switchType(QRType.TEXT)
    }

    private fun initViews() {
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
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupListeners() {
        // 类型切换监听
        btnTypeText.setOnClickListener { switchType(QRType.TEXT) }
        btnTypeUrl.setOnClickListener { switchType(QRType.URL) }

        btnGenerate.setOnClickListener {
            var content = etContent.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入要生成二维码的内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // URL类型验证和自动补全 (仅针对 URL 模式)
            if (currentType == QRType.URL) {
                if (!isValidUrl(content)) {
                    // 尝试自动补全
                    if (!content.startsWith("http://") && !content.startsWith("https://")) {
                        content = "https://$content"
                        etContent.setText(content)
                    } else {
                        Toast.makeText(this, "请输入有效的网址", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
            }

            generateQRCode(content)
        }

        btnSave.setOnClickListener {
            // Android 10 (API 29) 及以上通常不需要写权限即可保存到相册
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveQRCodeToGallery()
            } else {
                if (checkStoragePermission()) {
                    saveQRCodeToGallery()
                } else {
                    requestStoragePermission()
                }
            }
        }

        btnShare.setOnClickListener {
            shareQRCode()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun switchType(type: QRType) {
        currentType = type

        // 切换类型时，隐藏之前的生成结果，避免混淆
        if (::cardResult.isInitialized) {
            cardResult.visibility = View.GONE
        }

        // 更新按钮颜色
        val selectedColor = getColor(android.R.color.holo_blue_dark)
        val unselectedColor = getColor(android.R.color.darker_gray)

        btnTypeText.setBackgroundColor(if (type == QRType.TEXT) selectedColor else unselectedColor)
        btnTypeUrl.setBackgroundColor(if (type == QRType.URL) selectedColor else unselectedColor)

        // 按钮文字颜色反转逻辑（可选，为了更好的视觉效果）
        btnTypeText.setTextColor(if (type == QRType.TEXT) Color.WHITE else Color.parseColor("#666666"))
        btnTypeUrl.setTextColor(if (type == QRType.URL) Color.WHITE else Color.parseColor("#666666"))

        // 更新界面显示
        when (type) {
            QRType.TEXT -> {
                etContent.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            }
            QRType.URL -> {
                etContent.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
            }
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val pattern = Regex("^(http://|https://).+")
            pattern.matches(url) && url.contains(".")
        } catch (e: Exception) {
            false
        }
    }

    private fun generateQRCode(content: String) {
        try {
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1
            hints[EncodeHintType.ERROR_CORRECTION] = com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M

            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                QR_CODE_SIZE,
                QR_CODE_SIZE,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            currentQRBitmap = bitmap
            ivQRCode.setImageBitmap(bitmap)

            // 显示包含所有结果元素的 CardView
            cardResult.visibility = View.VISIBLE

            Toast.makeText(this, "二维码生成成功", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "生成失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun checkStoragePermission(): Boolean {
        // Android 13+ 使用 READ_MEDIA_IMAGES
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12及以下使用 WRITE_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                STORAGE_PERMISSION_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
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

        val filename = "QR_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRTool")
                }

                val contentResolver = contentResolver
                imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { contentResolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
                imageUri = Uri.fromFile(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Toast.makeText(this, "已保存到相册", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun shareQRCode() {
        val bitmap = currentQRBitmap
        if (bitmap == null) {
            Toast.makeText(this, "请先生成二维码", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 将Bitmap保存到缓存目录用于分享
            val cachePath = File(externalCacheDir, "my_images/")
            cachePath.mkdirs()
            val file = File(cachePath, "share_qr.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()

            @Suppress("DEPRECATION")
            val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "QR Share", null)
            val uri = Uri.parse(path)

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)

            startActivity(Intent.createChooser(shareIntent, "分享二维码"))

        } catch (e: Exception) {
            Toast.makeText(this, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}