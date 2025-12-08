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
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class QRGenerateActivity : AppCompatActivity() {

    private lateinit var etContent: EditText
    private lateinit var btnGenerate: Button
    private lateinit var tvQRLabel: TextView
    private lateinit var ivQRCode: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnShare: Button
    private lateinit var btnBack: ImageButton
    
    private var currentQRBitmap: Bitmap? = null

    companion object {
        private const val STORAGE_PERMISSION_CODE = 102
        private const val QR_CODE_SIZE = 800
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_generate)

        etContent = findViewById(R.id.etContent)
        btnGenerate = findViewById(R.id.btnGenerate)
        tvQRLabel = findViewById(R.id.tvQRLabel)
        ivQRCode = findViewById(R.id.ivQRCode)
        btnSave = findViewById(R.id.btnSave)
        btnShare = findViewById(R.id.btnShare)
        btnBack = findViewById(R.id.btnBack)

        btnGenerate.setOnClickListener {
            val content = etContent.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入要生成二维码的内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            generateQRCode(content)
        }

        btnSave.setOnClickListener {
            if (checkStoragePermission()) {
                saveQRCodeToGallery()
            } else {
                requestStoragePermission()
            }
        }

        btnShare.setOnClickListener {
            shareQRCode()
        }

        btnBack.setOnClickListener {
            navigateToHome()
        }
    }

    private fun generateQRCode(content: String) {
        try {
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1

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
            
            tvQRLabel.visibility = View.VISIBLE
            ivQRCode.visibility = View.VISIBLE
            btnSave.visibility = View.VISIBLE
            btnShare.visibility = View.VISIBLE

            Toast.makeText(this, "二维码生成成功", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "生成失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
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
        val bitmap = currentQRBitmap
        if (bitmap == null) {
            Toast.makeText(this, "请先生成二维码", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val filename = "QRCode_${System.currentTimeMillis()}.png"
            var fos: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                Toast.makeText(this, "二维码已保存到相册", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareQRCode() {
        val bitmap = currentQRBitmap
        if (bitmap == null) {
            Toast.makeText(this, "请先生成二维码", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val cachePath = File(cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "qrcode_share.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()

            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "分享二维码"))
        } catch (e: Exception) {
            Toast.makeText(this, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
