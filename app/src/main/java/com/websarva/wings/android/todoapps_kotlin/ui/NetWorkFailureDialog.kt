package com.websarva.wings.android.todoapps_kotlin.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.Exception
import java.lang.IllegalStateException

class NetWorkFailureDialog(private var flag: Boolean): DialogFragment() {
    private var listener: DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = activity?.let {
            val builder = AlertDialog.Builder(it)
                .setTitle("注意")
                .setMessage("この端末は現在、ネットワークに接続されていません！\nオフライン状態で続行しますか？\n(オフライン状態だと保存したデータは他の端末と共有されません)")
            // trueがMainActivityからの呼び出し
            if (flag){
                builder.setPositiveButton("YES"){_, _ ->
                    listener?.onDialogReceive(flag = true)
                }
                builder.setNegativeButton("NO"){_, _ ->
                }
            }else{
                builder.setPositiveButton("OK"){_, _ ->
                }
            }
                builder.create()

        }
        if (!flag){
            this.isCancelable = false
        }

        return dialog?: throw IllegalStateException("activityがnullです")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as DialogListener
        }catch (e: Exception){
            Log.wtf("ERROR", "CANNOT FIND LISTENER")
        }
    }

    override fun onDetach() {
        listener = null

        super.onDetach()
    }
}