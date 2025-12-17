package com.chy.qmzy_202308190231.viewmodel

class OneTimeEvent<out T>(private val content: T) {
    private var hasBeenHandled: Boolean = false

    fun getContentIfNotHandled(): T? {
        if (hasBeenHandled) return null
        hasBeenHandled = true
        return content
    }

    fun peekContent(): T = content
}
