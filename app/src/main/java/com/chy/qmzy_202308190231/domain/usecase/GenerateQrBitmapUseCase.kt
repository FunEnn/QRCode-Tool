package com.chy.qmzy_202308190231.domain.usecase

import android.graphics.Bitmap
import com.chy.qmzy_202308190231.domain.qrcode.QrBitmapGenerator

class GenerateQrBitmapUseCase(
    private val generator: QrBitmapGenerator
) {
    fun execute(content: String, size: Int): Bitmap? = generator.generate(content, size)
}
