package com.websarva.wings.android.todoapps_kotlin.ui

interface OnPreferenceListener{
    fun onPreferenceWriteListener(position: Int, keyName: String, checkFlag: Boolean)
    fun onPreferenceReadListener(keyName: String): Boolean
}