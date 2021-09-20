package com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo

class TodoListEvent<out: MutableList<MutableMap<String, String>>>(private val content: MutableList<MutableMap<String, String>>) {
    var hasBeenHandled = false
        private set

    val contentIfNotHandled: MutableList<MutableMap<String, String>>?
        get() {
            return if (hasBeenHandled){
                null
            }else{
                hasBeenHandled = true
                content
            }
        }

    var peekContent: MutableList<MutableMap<String, String>> = content
}