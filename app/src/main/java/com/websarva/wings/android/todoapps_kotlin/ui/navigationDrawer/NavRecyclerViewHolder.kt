package com.websarva.wings.android.todoapps_kotlin.ui.navigationDrawer

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R

class NavRecyclerViewHolder(var view: View): RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.navTitle)
    val count: TextView = view.findViewById(R.id.navUnCompleteTaskCount)
}