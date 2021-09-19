package com.websarva.wings.android.todoapps_kotlin.ui

import androidx.fragment.app.DialogFragment

interface DialogListener {
    fun onDialogFlagReceive(
        dialog: DialogFragment,
        list: String,
        type: Int,
        flag: Boolean,
        position: Int?
    )

    fun onDialogReceive(flag: Boolean)
}