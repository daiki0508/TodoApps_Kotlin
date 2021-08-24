package com.websarva.wings.android.todoapps_kotlin.ui

import android.view.View

interface OnItemClickListener {
    fun onItemClickListener(view: View, position: Int, list: String?)
}