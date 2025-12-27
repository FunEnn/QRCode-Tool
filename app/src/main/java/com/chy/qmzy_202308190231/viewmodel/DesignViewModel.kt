package com.chy.qmzy_202308190231.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chy.qmzy_202308190231.domain.usecase.ApplyQrDesignUseCase

class DesignViewModel(
    private val applyQrDesignUseCase: ApplyQrDesignUseCase
) : ViewModel() {

    data class UiState(
        val preview: Bitmap? = null
    )

    sealed class Event {
        data class Toast(val message: String) : Event()
        data class RequestSave(val bitmap: Bitmap) : Event()
    }

    private val _uiState = MutableLiveData(UiState())
    val uiState: LiveData<UiState> = _uiState

    private val _event = MutableLiveData<OneTimeEvent<Event>>()
    val event: LiveData<OneTimeEvent<Event>> = _event

    private var baseQrBitmap: Bitmap? = null
    private var selectedColor: Int = android.graphics.Color.BLACK
    private var logoBitmap: Bitmap? = null

    fun setBaseQrBitmap(bitmap: Bitmap) {
        baseQrBitmap = bitmap
        recomputePreview()
    }

    fun setSelectedColor(color: Int) {
        selectedColor = color
        recomputePreview()
    }

    fun setLogoBitmap(bitmap: Bitmap?) {
        logoBitmap = bitmap
        recomputePreview()
    }

    fun onSaveClicked() {
        val bitmap = _uiState.value?.preview
        if (bitmap == null) {
            _event.value = OneTimeEvent(Event.Toast("请先上传二维码图片"))
            return
        }
        _event.value = OneTimeEvent(Event.RequestSave(bitmap))
    }

    private fun recomputePreview() {
        val base = baseQrBitmap ?: return
        val finalBitmap = applyQrDesignUseCase.execute(base, selectedColor, logoBitmap)
        _uiState.value = UiState(preview = finalBitmap)
    }
}
