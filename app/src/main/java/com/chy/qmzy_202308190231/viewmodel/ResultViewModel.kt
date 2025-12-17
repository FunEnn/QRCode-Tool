package com.chy.qmzy_202308190231.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chy.qmzy_202308190231.utils.UrlUtils

class ResultViewModel : ViewModel() {

    data class UiState(
        val format: String = "未知",
        val content: String = "",
        val showOpenLink: Boolean = false
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
        _uiState.value = UiState(
            format = format,
            content = content,
            showOpenLink = UrlUtils.isUrl(content)
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
        if (text.isNotEmpty()) {
            _event.value = OneTimeEvent(Event.OpenLink(UrlUtils.normalizeUrl(text)))
        }
    }
}
