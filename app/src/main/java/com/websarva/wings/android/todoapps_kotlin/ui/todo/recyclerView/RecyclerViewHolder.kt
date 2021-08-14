package com.websarva.wings.android.todoapps_kotlin.ui.todo.recyclerView

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R

class RecyclerViewHolder(var view: View): RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.title)
    //val contents: TextView = view.findViewById(R.id.content)
    val rvContents: RecyclerView = view.findViewById(R.id.rvContents)
}