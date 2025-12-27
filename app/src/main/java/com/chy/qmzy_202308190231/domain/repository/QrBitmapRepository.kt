package com.chy.qmzy_202308190231.domain.repository

import android.graphics.Bitmap

interface QrBitmapRepository {
    fun generateQrBitmap(content: String, size: Int): Bitmap?
}
