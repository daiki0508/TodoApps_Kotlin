package com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo

class DeleteAllEvent<out: Boolean>(private val content: Boolean) {
    var hasBeenHandled = false
        private set

    val contentIfNotHandled: Boolean?
        get() {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

    var peekContent: Boolean = content
}