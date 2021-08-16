package com.websarva.wings.android.todoapps_kotlin

import androidx.fragment.app.DialogFragment

interface DialogListener {
    fun onDialogFlagReceive(dialog: DialogFragment, list: String, type: Int)
}