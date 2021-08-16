package com.websarva.wings.android.todoapps_kotlin.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.websarva.wings.android.todoapps_kotlin.DialogListener
import com.websarva.wings.android.todoapps_kotlin.R

class AddListDialog(private var flag: Boolean, private var type: Int): DialogFragment() {
    private var listener: DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = Dialog(requireActivity())
        builder.window.apply {
            this?.requestFeature(Window.FEATURE_NO_TITLE)
            this?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            this?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        builder.apply {
            setContentView(R.layout.dialog_custom)
            // typeは0が追加、1が更新とする
            if (flag && type == 0){
                findViewById<TextView>(R.id.tvList).text = "タスクの追加"
            }else if (flag && type == 1){
                findViewById<TextView>(R.id.tvList).text = "タスクの更新"
            }else if (!flag && type == 1){
                findViewById<TextView>(R.id.tvList).text = "リストの更新"
            }
            findViewById<View>(R.id.positive_button).setOnClickListener {
                val list = Editable.Factory.getInstance().newEditable(findViewById<EditText>(R.id.edList).text)
                listener?.onDialogFlagReceive(this@AddListDialog, list.toString(), type)
                list.clear()
                dismiss()
            }
            this.findViewById<View>(R.id.close_button).setOnClickListener {
                dismiss()
            }
        }
        this.isCancelable = false

        return builder
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as DialogListener
        }catch (e: Exception){
            Log.e("ERROR", "CANNOT FIND LISTENER")
        }
    }
}