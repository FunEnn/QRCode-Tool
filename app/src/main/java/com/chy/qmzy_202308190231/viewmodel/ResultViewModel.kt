package com.chy.qmzy_202308190231.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chy.qmzy_202308190231.utils.UrlUtils

class ResultViewModel : ViewModel() {

    data class UiState(
        val format: String = "未知",
        val content: String = "",
        val showOpenLink: Boolean = false,
        val openActionText: String = "打开"
    )

    sealed class Event {
        data class Copy(val text: String) : Event()
        data class OpenLink(val url: String) : Event()
    }

    private val _uiState = MutableLiveData(UiState())
    val uiState: LiveData<UiState> = _uiState

    private val _event = MutableLiveData<OneTimeEvent<Event>>()
    val event: LiveData<OneTimeEvent<Event>> = _event

    fun init(format: String, content: String) {
        val action = resolveOpenAction(content)
        _uiState.value = UiState(
            format = format,
            content = content,
            showOpenLink = action != null,
            openActionText = action?.label ?: "打开"
        )
    }

    fun onCopyClicked() {
        val text = _uiState.value?.content ?: ""
        if (text.isNotEmpty()) {
            _event.value = OneTimeEvent(Event.Copy(text))
        }
    }

    fun onOpenLinkClicked() {
        val text = _uiState.value?.content ?: ""
        if (text.isEmpty()) return
        val action = resolveOpenAction(text) ?: return
        _event.value = OneTimeEvent(Event.OpenLink(action.uri))
    }

    private data class OpenAction(
        val uri: String,
        val label: String
    )

    private fun resolveOpenAction(raw: String): OpenAction? {
        val text = raw.trim()
        if (text.isEmpty()) return null

        if (text.startsWith("tel:", ignoreCase = true)) {
            return OpenAction(uri = text, label = "拨号")
        }
        if (text.startsWith("mailto:", ignoreCase = true)) {
            return OpenAction(uri = text, label = "发邮件")
        }

        // 支持用户扫描到的是纯电话/邮箱
        val phone = text.replace(" ", "").replace("-", "")
        if (Regex("^[+]?\\d{3,20}$").matches(phone)) {
            return OpenAction(uri = "tel:$phone", label = "拨号")
        }
        if (Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE).matches(text)) {
            return OpenAction(uri = "mailto:$text", label = "发邮件")
        }

        if (UrlUtils.isUrl(text)) {
            return OpenAction(uri = UrlUtils.normalizeUrl(text), label = "打开链接")
        }
        return null
    }
}
