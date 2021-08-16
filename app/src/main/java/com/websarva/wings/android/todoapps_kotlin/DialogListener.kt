package com.websarva.wings.android.todoapps_kotlin

import androidx.fragment.app.DialogFragment
import com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView.ChildRecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView.RecyclerViewAdapter

interface DialogListener {
    fun onDialogFlagReceive(
        dialog: DialogFragment,
        list: String,
        type: Int,
        flag: Boolean,
        position: Int?
    )
}