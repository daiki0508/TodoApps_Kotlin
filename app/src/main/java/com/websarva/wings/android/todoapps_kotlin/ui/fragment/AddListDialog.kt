package com.websarva.wings.android.todoapps_kotlin.ui.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.model.DialogBundle

class AddListDialog(
    private var flag: Boolean,
    private var type: Int,
    private var position: Int?
    ): DialogFragment() {
    //private var listener: DialogListener? = null

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

                // 結果を送信
                parentFragmentManager.setFragmentResult(DialogBundle.Result.name, Bundle().apply {
                    // bundleに値をセット
                    if (type == 0 && !flag){
                        // listの追加
                        this.putString(DialogBundle.List.name, list.toString())
                    }else if (type == 1){
                        // task・listの更新
                        this.putString(DialogBundle.List.name, list.toString())
                        this.putInt(DialogBundle.Type.name, type)
                        this.putBoolean(DialogBundle.Flag.name, flag)
                        this.putInt(DialogBundle.Position.name, position!!)
                    }else{
                        // taskの追加
                        this.putString(DialogBundle.List.name, list.toString())
                        this.putInt(DialogBundle.Type.name, type)
                        this.putBoolean(DialogBundle.Flag.name, flag)
                    }
                })

                // listの削除
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

    /*override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = targetFragment as DialogListener
        }catch (e: Exception){
            Log.wtf("ERROR", "CANNOT FIND LISTENER")
        }
    }*/
}