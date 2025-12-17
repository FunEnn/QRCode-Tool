package com.chy.qmzy_202308190231.domain.qrcode

import android.graphics.Bitmap

interface QrBitmapGenerator {
    fun generate(content: String, size: Int): Bitmap?
}
