package com.websarva.wings.android.todoapps_kotlin.ui.todo.recyclerView

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R

class NavRecyclerViewAdapter(private var items: MutableList<MutableMap<String, String>>): RecyclerView.Adapter<NavRecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.row_nav, parent, false)

        return NavRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: NavRecyclerViewHolder, position: Int) {
        holder.title.text = items[position]["list"]
        holder.count.text = "0"

        // テスト用
        if (position == 0){
            holder.view.setBackgroundColor(Color.LTGRAY)
        }else{
            holder.view.setBackgroundColor(Color.WHITE)
        }

        holder.view.setOnClickListener {
            TODO("未実装")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}