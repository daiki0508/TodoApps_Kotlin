package com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R

interface OnChildItemClickListener {
    fun onItemClickListener(view: View, position: Int)
}

class ChildRecyclerViewAdapter(private var items: MutableList<MutableMap<String, String>>): RecyclerView.Adapter<ChildRecyclerViewHolder>() {
    private lateinit var listener: OnChildItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view =  layoutInflater.inflate(R.layout.row_child_top, parent, false)

        return ChildRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildRecyclerViewHolder, position: Int) {
        holder.content.textSize = 22F
        val mlp = holder.content.layoutParams as ViewGroup.MarginLayoutParams
        mlp.setMargins(mlp.leftMargin, 15, mlp.rightMargin, 15)
        holder.content.text = items[position]["task"]

        holder.view.setOnClickListener {
            TODO("未実装")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setOnItemClickListener(listener: OnChildItemClickListener){
        this.listener = listener
    }
}