package com.chy.qmzy_202308190231.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chy.qmzy_202308190231.viewmodel.DesignViewModel
import com.chy.qmzy_202308190231.viewmodel.GenerateViewModel
import com.chy.qmzy_202308190231.viewmodel.ResultViewModel
import com.chy.qmzy_202308190231.viewmodel.ScanViewModel

class AppViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(GenerateViewModel::class.java) -> {
                GenerateViewModel(appContainer.generateQrBitmapUseCase) as T
            }
            modelClass.isAssignableFrom(DesignViewModel::class.java) -> {
                DesignViewModel(appContainer.applyQrDesignUseCase) as T
            }
            modelClass.isAssignableFrom(ScanViewModel::class.java) -> {
                ScanViewModel() as T
            }
            modelClass.isAssignableFrom(ResultViewModel::class.java) -> {
                ResultViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
