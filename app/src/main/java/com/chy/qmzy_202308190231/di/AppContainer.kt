package com.chy.qmzy_202308190231.di

import com.chy.qmzy_202308190231.data.repository.ZxingQrBitmapRepository
import com.chy.qmzy_202308190231.domain.repository.QrBitmapRepository
import com.chy.qmzy_202308190231.domain.usecase.ApplyQrDesignUseCase
import com.chy.qmzy_202308190231.domain.usecase.GenerateQrBitmapUseCase

class AppContainer {
    val qrBitmapRepository: QrBitmapRepository by lazy {
        ZxingQrBitmapRepository()
    }

    val generateQrBitmapUseCase: GenerateQrBitmapUseCase by lazy {
        GenerateQrBitmapUseCase(qrBitmapRepository)
    }

    val applyQrDesignUseCase: ApplyQrDesignUseCase by lazy {
        ApplyQrDesignUseCase()
    }
}
