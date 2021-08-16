package com.websarva.wings.android.todoapps_kotlin.ui.todo.recyclerView

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R

interface OnChildItemClickListener {
    fun onItemClickListener(view: View, position: Int)
}

class ChildRecyclerViewAdapter(private var items: MutableList<MutableMap<String, String>>, private var position: Int): RecyclerView.Adapter<ChildRecyclerViewHolder>() {
    private lateinit var listener: OnChildItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view =  layoutInflater.inflate(R.layout.row_child_top, parent, false)

        return ChildRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildRecyclerViewHolder, position: Int) {
        holder.content.ellipsize = TextUtils.TruncateAt.END
        holder.content.text = items[position]["task"]

        holder.checkBox.visibility = View.GONE

        holder.view.setOnClickListener {
            listener.onItemClickListener(it, this.position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setOnItemClickListener(listener: OnChildItemClickListener){
        this.listener = listener
    }
}