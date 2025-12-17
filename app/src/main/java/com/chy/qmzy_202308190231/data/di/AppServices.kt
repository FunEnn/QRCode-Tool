package com.chy.qmzy_202308190231.data.di

import com.chy.qmzy_202308190231.data.qrcode.ZxingQrBitmapGenerator
import com.chy.qmzy_202308190231.domain.usecase.ApplyQrDesignUseCase
import com.chy.qmzy_202308190231.domain.usecase.GenerateQrBitmapUseCase

object AppServices {
    val generateQrBitmapUseCase: GenerateQrBitmapUseCase by lazy {
        GenerateQrBitmapUseCase(ZxingQrBitmapGenerator())
    }

    val applyQrDesignUseCase: ApplyQrDesignUseCase by lazy {
        ApplyQrDesignUseCase()
    }
}
