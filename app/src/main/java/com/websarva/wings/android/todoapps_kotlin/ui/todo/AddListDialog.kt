package com.websarva.wings.android.todoapps_kotlin.ui.todo

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.websarva.wings.android.todoapps_kotlin.R

class AddListDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = Dialog(requireActivity())
        builder.window.apply {
            this?.requestFeature(Window.FEATURE_NO_TITLE)
            this?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            this?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        builder.apply {
            this.setContentView(R.layout.dialog_custom)
            this.findViewById<View>(R.id.positive_button).setOnClickListener {
                TODO("テスト")
            }
            this.findViewById<View>(R.id.close_button).setOnClickListener {
                dismiss()
            }
        }
        this.isCancelable = false

        return builder
    }
}