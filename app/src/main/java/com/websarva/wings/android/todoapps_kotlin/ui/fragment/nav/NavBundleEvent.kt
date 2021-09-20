package com.websarva.wings.android.todoapps_kotlin.ui.fragment.nav

import android.os.Bundle

class NavBundleEvent<out: Bundle>(private val content: Bundle) {
    var hasBeenHandled = false
        private set

    val contentIfNotHandled: Bundle?
        get() {
            return if (hasBeenHandled){
                null
            }else{
                hasBeenHandled = true
                content
            }
        }

    val peekContent: Bundle = content
}