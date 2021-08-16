package com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.ui.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.add.AddTodoTaskActivity
import com.websarva.wings.android.todoapps_kotlin.viewModel.AddTodoTaskViewModel

interface OnChildItemClickListener {
    fun onItemClickListener(view: View, position: Int)
}

class ChildRecyclerViewAdapter(
    private var items: MutableList<MutableMap<String, String>>,
    ): RecyclerView.Adapter<ChildRecyclerViewHolder>() {
    private lateinit var listener: OnChildItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view =  layoutInflater.inflate(R.layout.row_child_top, parent, false)

        return ChildRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildRecyclerViewHolder, position: Int) {
        holder.content.textSize = 22F
        holder.content.isSingleLine = false
        val mlp = holder.content.layoutParams as ViewGroup.MarginLayoutParams
        mlp.setMargins(mlp.leftMargin, 15, mlp.rightMargin, 15)
        holder.content.text = items[position]["task"]

        holder.content.setOnClickListener {
            /*viewModel.setPosition(position, items.size)
            AddListDialog(flag = true, type = 1).show(activity.supportFragmentManager, "UpdateTaskDialog")*/
            listener.onItemClickListener(it, position)
        }

        holder.checkBox.setOnClickListener {
            holder.content.apply {
                if (holder.checkBox.isChecked){
                    setTextColor(Color.LTGRAY)
                    paint.flags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    paint.isAntiAlias = true
                }else{
                    setTextColor(Color.GRAY)
                    paint.flags = Paint.ANTI_ALIAS_FLAG
                    paint.isAntiAlias = false
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setOnItemClickListener(listener: OnChildItemClickListener){
        this.listener = listener
    }
}