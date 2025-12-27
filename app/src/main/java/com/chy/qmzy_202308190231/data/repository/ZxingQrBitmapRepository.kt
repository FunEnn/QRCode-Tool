package com.chy.qmzy_202308190231.data.repository

import android.graphics.Bitmap
import com.chy.qmzy_202308190231.data.qrcode.ZxingQrBitmapGenerator
import com.chy.qmzy_202308190231.domain.repository.QrBitmapRepository

class ZxingQrBitmapRepository : QrBitmapRepository {
    private val generator = ZxingQrBitmapGenerator()

    override fun generateQrBitmap(content: String, size: Int): Bitmap? {
        return generator.generate(content, size)
    }
}
