package com.websarva.wings.android.todoapps_kotlin.ui.main

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.Exception
import java.lang.IllegalStateException

interface DialogListener{
    fun onDialogReceive(flag: Boolean)
}

class NetWorkFailureDialog: DialogFragment() {
    private var listener: DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = activity?.let {
            AlertDialog.Builder(it)
                .setTitle("注意")
                .setMessage("この端末は現在、ネットワークに接続されていません！\nオフライン状態で続行しますか？\n(オフライン状態だと保存したデータは他の端末と共有されません)")
                .setPositiveButton("YES"){_, _ ->
                    listener?.onDialogReceive(flag = true)
                }
                .setNegativeButton("NO"){_, _ ->

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