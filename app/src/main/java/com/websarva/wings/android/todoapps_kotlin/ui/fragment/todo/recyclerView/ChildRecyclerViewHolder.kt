package com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.recyclerView

import android.view.ContextMenu
import android.view.Menu
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R

class ChildRecyclerViewHolder(var view: View): RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
    val content: TextView = view.findViewById(R.id.content)
    val checkBox: CheckBox = view.findViewById(R.id.checkbox)

    val contentView: View = view.findViewById(R.id.content)

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        view: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menu!!.add(Menu.NONE, 1, Menu.NONE, R.string.context_deleteList_title)
    }
}