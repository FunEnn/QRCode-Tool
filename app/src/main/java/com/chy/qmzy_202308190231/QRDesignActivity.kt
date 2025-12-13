package com.chy.qmzy_202308190231

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class QRDesignActivity : AppCompatActivity() {

    // UI 组件
    private lateinit var ivPreview: ImageView
    private lateinit var btnAddLogo: Button
    private lateinit var btnGenerate: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnUploadQrCode: Button 

    // 颜色色板网格
    private lateinit var colorPaletteGrid: GridLayout

    // 状态变量
    private var selectedColor = Color.BLACK
    private var logoBitmap: Bitmap? = null
    private var currentGeneratedBitmap: Bitmap? = null
    private var originalUploadedQrBitmap: Bitmap? = null 

    // 图片选择器 for logo
    private val imagePickerForLogoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleSelectedLogo(it)
        }
    }

    // 图片选择器 for QR code
    private val imagePickerForQrCodeLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleSelectedQrCode(it)
        }
    }

    companion object {
        private const val QR_CODE_SIZE = 800
        private const val STORAGE_PERMISSION_CODE = 103
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_design)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        ivPreview = findViewById(R.id.ivPreview)
        btnAddLogo = findViewById(R.id.btnAddLogo)
        btnGenerate = findViewById(R.id.btnGenerate)
        btnBack = findViewById(R.id.btnBack)
        btnUploadQrCode = findViewById(R.id.btnUploadQrCode)

        colorPaletteGrid = findViewById(R.id.colorPaletteGrid)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        // 色板选择逻辑
        for (i in 0 until colorPaletteGrid.childCount) {
            val colorView = colorPaletteGrid.getChildAt(i)
            colorView.setOnClickListener {
                val colorTag = colorView.tag?.toString()
                colorTag?.let {
                    try {
                        selectedColor = Color.parseColor(it)
                        applyDesignToQrCode()
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        Toast.makeText(this, "颜色解析错误: $it", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Logo 上传
        btnAddLogo.setOnClickListener {
            imagePickerForLogoLauncher.launch("image/*")
        }

        // Upload QR Code
        btnUploadQrCode.setOnClickListener {
            imagePickerForQrCodeLauncher.launch("image/*")
        }

        // 保存二维码按钮
        btnGenerate.setOnClickListener {
            if (currentGeneratedBitmap != null) {
                checkPermissionAndSave()
            } else {
                Toast.makeText(this, "请先上传二维码图片", Toast.LENGTH_SHORT).show()
            }
        }

        // 点击预览图保存
        ivPreview.setOnClickListener {
            if (currentGeneratedBitmap != null) {
                checkPermissionAndSave()
            }
        }
    }

    private fun handleSelectedLogo(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            logoBitmap = scaleBitmap(bitmap, 200)
            btnAddLogo.text = "Logo 已选择"
            applyDesignToQrCode() 
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Logo 加载失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSelectedQrCode(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                originalUploadedQrBitmap = scaleBitmap(bitmap, QR_CODE_SIZE)
                currentGeneratedBitmap = originalUploadedQrBitmap 
                ivPreview.setImageBitmap(currentGeneratedBitmap)
                applyDesignToQrCode() 
            } else {
                Toast.makeText(this, "无法加载二维码图片", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "加载二维码失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyDesignToQrCode() {
        val baseQrBitmap = originalUploadedQrBitmap ?: run {
            Toast.makeText(this, "请先上传二维码图片", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val designedBitmap = baseQrBitmap.copy(Bitmap.Config.ARGB_8888, true)
            
            val width = designedBitmap.width
            val height = designedBitmap.height

            for (x in 0 until width) {
                for (y in 0 until height) {
                    val pixelColor = designedBitmap.getPixel(x, y)
                    if (Color.red(pixelColor) < 128 && Color.green(pixelColor) < 128 && Color.blue(pixelColor) < 128) {
                        designedBitmap.setPixel(x, y, selectedColor)
                    } else {
                        designedBitmap.setPixel(x, y, Color.WHITE)
                    }
                }
            }

            val finalBitmap = if (logoBitmap != null) {
                addLogo(designedBitmap, logoBitmap!!)
            } else {
                designedBitmap
            }

            currentGeneratedBitmap = finalBitmap
            ivPreview.setImageBitmap(finalBitmap)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "应用设计失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addLogo(qrBitmap: Bitmap, logo: Bitmap): Bitmap {
        val qrWidth = qrBitmap.width
        val qrHeight = qrBitmap.height

        val logoSize = (qrWidth * 0.2f).toInt()
        val scaledLogo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true)

        val combined = Bitmap.createBitmap(qrWidth, qrHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)

        canvas.drawBitmap(qrBitmap, 0f, 0f, null)

        val logoBackgroundSize = (logoSize * 1.1f).toInt()
        val bgLeft = (qrWidth - logoBackgroundSize) / 2f
        val bgTop = (qrHeight - logoBackgroundSize) / 2f
        val paint = android.graphics.Paint()
        paint.color = Color.WHITE
        canvas.drawRect(bgLeft, bgTop, bgLeft + logoBackgroundSize, bgTop + logoBackgroundSize, paint)

        val logoLeft = (qrWidth - logoSize) / 2f
        val logoTop = (qrHeight - logoSize) / 2f
        canvas.drawBitmap(scaledLogo, logoLeft, logoTop, null)

        return combined
    }

    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxSize && height <= maxSize) return bitmap

        val scale = maxSize.toFloat() / maxOf(width, height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun checkPermissionAndSave() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageToGallery()
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                saveImageToGallery()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImageToGallery()
        }
    }

    private fun saveImageToGallery() {
        val bitmap = currentGeneratedBitmap ?: return
        val filename = "DesignQR_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRTool")
                }
                val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { contentResolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Toast.makeText(this, "已保存到相册", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}