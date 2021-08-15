package com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R

class ChildRecyclerViewHolder(var view: View): RecyclerView.ViewHolder(view) {
    val content: TextView = view.findViewById(R.id.content)
}