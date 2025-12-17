package com.chy.qmzy_202308190231.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScanViewModel : ViewModel() {

    data class UiState(
        val hint: String = "正在识别..."
    )

    sealed class Event {
        data class NavigateToResult(val value: String, val format: String) : Event()
        data class Toast(val message: String) : Event()
    }

    private val _uiState = MutableLiveData(UiState())
    val uiState: LiveData<UiState> = _uiState

    private val _event = MutableLiveData<OneTimeEvent<Event>>()
    val event: LiveData<OneTimeEvent<Event>> = _event

    private var lastScanValue: String? = null
    private var lastScanTimeMs: Long = 0L

    fun onBarcodeDetected(value: String, format: String) {
        val now = System.currentTimeMillis()
        val isDuplicate = (value == lastScanValue) && (now - lastScanTimeMs < SCAN_DEBOUNCE_MS)
        if (isDuplicate) return

        lastScanValue = value
        lastScanTimeMs = now

        _uiState.value = UiState(hint = "扫描成功！\n内容: $value")
        _event.value = OneTimeEvent(Event.NavigateToResult(value = value, format = format))
    }

    fun onGalleryScanStarted() {
        _uiState.value = UiState(hint = "正在识别图片中的二维码...")
    }

    fun onGalleryScanNotFound() {
        _uiState.value = UiState(hint = "图片中未找到二维码")
    }

    fun onGalleryScanFailed(message: String) {
        _uiState.value = UiState(hint = "识别失败")
        _event.value = OneTimeEvent(Event.Toast("识别失败: $message"))
    }

    companion object {
        private const val SCAN_DEBOUNCE_MS = 1200L
    }
}
