package com.chy.qmzy_202308190231.viewmodel

import android.graphics.Bitmap
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chy.qmzy_202308190231.domain.usecase.GenerateQrBitmapUseCase

class GenerateViewModel(
    private val generateQrBitmapUseCase: GenerateQrBitmapUseCase
) : ViewModel() {

    enum class QrType {
        TEXT, URL, PHONE, EMAIL
    }

    data class UiState(
        val type: QrType = QrType.TEXT,
        val bitmap: Bitmap? = null
    )

    sealed class Event {
        data class Toast(val message: String) : Event()
        data class RequestSave(val bitmap: Bitmap) : Event()
        data class RequestShare(val bitmap: Bitmap) : Event()
        data class UpdateInput(val text: String) : Event()
    }

    private val _uiState = MutableLiveData(UiState())
    val uiState: LiveData<UiState> = _uiState

    private val _event = MutableLiveData<OneTimeEvent<Event>>()
    val event: LiveData<OneTimeEvent<Event>> = _event

    fun setType(type: QrType) {
        _uiState.value = _uiState.value?.copy(type = type, bitmap = null) ?: UiState(type = type)
    }

    fun onGenerateClicked(rawInput: String) {
        var content = rawInput.trim()
        if (content.isEmpty()) {
            _event.value = OneTimeEvent(Event.Toast("请输入要生成二维码的内容"))
            return
        }

        val current = _uiState.value ?: UiState()
        when (current.type) {
            QrType.URL -> {
                if (!isValidUrl(content)) {
                    if (!content.startsWith("http://") && !content.startsWith("https://")) {
                        content = "https://$content"
                        _event.value = OneTimeEvent(Event.UpdateInput(content))
                    } else {
                        _event.value = OneTimeEvent(Event.Toast("请输入有效的网址"))
                        return
                    }
                }
            }
            QrType.PHONE -> {
                val normalized = normalizePhone(content)
                if (normalized == null) {
                    _event.value = OneTimeEvent(Event.Toast("请输入有效的手机号/电话"))
                    return
                }
                content = normalized
            }
            QrType.EMAIL -> {
                val normalized = normalizeEmail(content)
                if (normalized == null) {
                    _event.value = OneTimeEvent(Event.Toast("请输入有效的邮箱"))
                    return
                }
                content = normalized
            }
            QrType.TEXT -> Unit
        }

        val bitmap = generateQrBitmapUseCase.execute(content, QR_CODE_SIZE)
        if (bitmap == null) {
            _event.value = OneTimeEvent(Event.Toast("生成失败"))
            return
        }

        _uiState.value = current.copy(bitmap = bitmap)
        _event.value = OneTimeEvent(Event.Toast("二维码生成成功"))
    }

    fun onSaveClicked() {
        val bitmap = _uiState.value?.bitmap
        if (bitmap == null) {
            _event.value = OneTimeEvent(Event.Toast("请先生成二维码"))
            return
        }
        _event.value = OneTimeEvent(Event.RequestSave(bitmap))
    }

    fun onShareClicked() {
        val bitmap = _uiState.value?.bitmap
        if (bitmap == null) {
            _event.value = OneTimeEvent(Event.Toast("请先生成二维码"))
            return
        }
        _event.value = OneTimeEvent(Event.RequestShare(bitmap))
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val pattern = Regex("^(http://|https://).+")
            pattern.matches(url) && url.contains(".")
        } catch (_: Exception) {
            false
        }
    }

    private fun normalizePhone(input: String): String? {
        val t = input.trim()
        val raw = if (t.startsWith("tel:", ignoreCase = true)) t.substringAfter(":").trim() else t
        val phone = raw.replace(" ", "").replace("-", "")
        if (phone.isEmpty()) return null
        if (!Regex("^[+]?\\d{3,20}$").matches(phone)) return null
        return "tel:$phone"
    }

    private fun normalizeEmail(input: String): String? {
        val t = input.trim()
        val raw = if (t.startsWith("mailto:", ignoreCase = true)) t.substringAfter(":").trim() else t
        if (raw.isEmpty()) return null
        if (!Patterns.EMAIL_ADDRESS.matcher(raw).matches()) return null
        return "mailto:$raw"
    }

    companion object {
        private const val QR_CODE_SIZE = 800
    }
}
