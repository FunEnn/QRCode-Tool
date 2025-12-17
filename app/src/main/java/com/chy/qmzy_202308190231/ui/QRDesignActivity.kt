package com.chy.qmzy_202308190231.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chy.qmzy_202308190231.utils.ImageStoreUtils
import com.chy.qmzy_202308190231.R
import com.chy.qmzy_202308190231.viewmodel.DesignViewModel

class QRDesignActivity : AppCompatActivity() {

   // UI 组件
   private lateinit var ivPreview: ImageView
   private lateinit var btnAddLogo: Button
   private lateinit var btnGenerate: Button
   private lateinit var btnBack: ImageButton
   private lateinit var btnUploadQrCode: Button

   // 颜色色板网格
   private lateinit var colorPaletteGrid: GridLayout

   private val viewModel: DesignViewModel by viewModels()

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

       viewModel.uiState.observe(this) { state ->
           if (state.preview != null) {
               ivPreview.setImageBitmap(state.preview)
           }
       }

       viewModel.event.observe(this) { wrapper ->
           val event = wrapper?.getContentIfNotHandled() ?: return@observe
           when (event) {
               is DesignViewModel.Event.Toast -> {
                   Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
               }
               is DesignViewModel.Event.RequestSave -> {
                   checkPermissionAndSave(event.bitmap)
               }
           }
       }
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
                       viewModel.setSelectedColor(Color.parseColor(it))
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
           viewModel.onSaveClicked()
       }

       // 点击预览图保存
       ivPreview.setOnClickListener {
           viewModel.onSaveClicked()
       }
   }

   private fun handleSelectedLogo(uri: Uri) {
       try {
           val inputStream = contentResolver.openInputStream(uri)
           val bitmap = BitmapFactory.decodeStream(inputStream)
           inputStream?.close()

           val logo = scaleBitmap(bitmap, 200)
           btnAddLogo.text = "Logo 已选择"
           viewModel.setLogoBitmap(logo)
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
               val base = scaleBitmap(bitmap, QR_CODE_SIZE)
               viewModel.setBaseQrBitmap(base)
           } else {
               Toast.makeText(this, "无法加载二维码图片", Toast.LENGTH_SHORT).show()
           }
       } catch (e: Exception) {
           e.printStackTrace()
           Toast.makeText(this, "加载二维码失败: ${e.message}", Toast.LENGTH_SHORT).show()
       }
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

   private fun checkPermissionAndSave(bitmap: Bitmap) {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
           saveImageToGallery(bitmap)
       } else {
           if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
               == PackageManager.PERMISSION_GRANTED) {
               saveImageToGallery(bitmap)
           } else {
               pendingSaveBitmap = bitmap
               ActivityCompat.requestPermissions(
                   this,
                   arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                   STORAGE_PERMISSION_CODE
               )
           }
       }
   }

   private var pendingSaveBitmap: Bitmap? = null

   override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
       super.onRequestPermissionsResult(requestCode, permissions, grantResults)
       if (requestCode == STORAGE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
           pendingSaveBitmap?.let { saveImageToGallery(it) }
           pendingSaveBitmap = null
       }
   }

   private fun saveImageToGallery(bitmap: Bitmap) {
       val filename = "DesignQR_${System.currentTimeMillis()}.jpg"
       try {
           val uri = ImageStoreUtils.saveJpegToGallery(this, bitmap, filename)
           if (uri != null) {
               Toast.makeText(this, "已保存到相册", Toast.LENGTH_SHORT).show()
           } else {
               Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
           }
       } catch (e: Exception) {
           Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
           e.printStackTrace()
       }
   }
}