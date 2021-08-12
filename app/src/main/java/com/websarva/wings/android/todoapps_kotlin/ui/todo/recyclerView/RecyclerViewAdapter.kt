package com.websarva.wings.android.todoapps_kotlin.ui.todo.recyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R

interface OnItemClickListener {
    fun onItemClickListener(view: View, position: Int, clickedText: String)
}

class RecyclerViewAdapter(private var items: MutableList<MutableMap<String, String>>): RecyclerView.Adapter<RecyclerViewHolder>() {
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.row_top, parent, false)

        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.view.setOnClickListener {
            TODO("Listenerの引数が未定のため")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}