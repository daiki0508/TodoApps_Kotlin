package com.websarva.wings.android.todoapps_kotlin.ui.settings

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.ui.DialogListener
import java.lang.Exception
import java.lang.IllegalStateException

class SelectModeDialog: DialogFragment() {
    private var listener: DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = activity?.let {
            AlertDialog.Builder(it)
                .setTitle("モード選択")
                .setItems(R.array.select_mode){_, which ->
                    // trueがバックアップ
                    if (which == 0){
                        listener?.onDialogReceive(flag = true)
                    }else{
                        listener?.onDialogReceive(flag = false)
                    }
                }
                .create()
        }

        return dialog?: throw IllegalStateException("activityがnullです")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as DialogListener
        }catch (e: Exception){
            Log.e("ERROR", "CANNOT FIND LISTENER")
        }
    }

    override fun onDetach() {
        listener = null

        super.onDetach()
    }
}