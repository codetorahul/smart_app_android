package com.example.smartapp.utils

// createdBy: Rahul Gupta on 11/26/2024 6:19 PM

open class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    /**
     * Returns the content if it has not been handled yet, otherwise returns null.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it has already been handled.
     */
    fun peekContent(): T = content
}
