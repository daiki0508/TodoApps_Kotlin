package com.websarva.wings.android.todoapps_kotlin.ui.fragment

import android.view.View

interface OnItemClickListener {
    fun onItemClickListener(view: View, position: Int, list: String?)
}