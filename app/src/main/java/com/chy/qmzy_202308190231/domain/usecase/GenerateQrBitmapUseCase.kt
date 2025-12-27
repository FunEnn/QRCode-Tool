package com.chy.qmzy_202308190231.domain.usecase

import android.graphics.Bitmap
import com.chy.qmzy_202308190231.domain.repository.QrBitmapRepository

class GenerateQrBitmapUseCase(
    private val repository: QrBitmapRepository
) {
    fun execute(content: String, size: Int): Bitmap? = repository.generateQrBitmap(content, size)
}
