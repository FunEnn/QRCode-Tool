package com.chy.qmzy_202308190231.domain.usecase

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color

class ApplyQrDesignUseCase {
    fun execute(base: Bitmap, selectedColor: Int, logo: Bitmap?): Bitmap {
        val designed = applyColor(base, selectedColor)
        return if (logo != null) addLogo(designed, logo) else designed
    }

    private fun applyColor(base: Bitmap, color: Int): Bitmap {
        val designedBitmap = base.copy(Bitmap.Config.ARGB_8888, true)
        val width = designedBitmap.width
        val height = designedBitmap.height

        val pixels = IntArray(width * height)
        designedBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixelColor = pixels[i]
            pixels[i] = if (
                Color.red(pixelColor) < 128 &&
                Color.green(pixelColor) < 128 &&
                Color.blue(pixelColor) < 128
            ) {
                color
            } else {
                Color.WHITE
            }
        }

        designedBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return designedBitmap
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
}
