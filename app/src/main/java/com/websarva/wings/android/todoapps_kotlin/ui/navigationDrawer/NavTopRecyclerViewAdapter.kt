package com.websarva.wings.android.todoapps_kotlin.ui.navigationDrawer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R

class NavTopRecyclerViewAdapter: RecyclerView.Adapter<NavTopRecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavTopRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.row_nav_all, parent, false)

        return NavTopRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: NavTopRecyclerViewHolder, position: Int) {
        holder.navIcon.setImageResource(R.drawable.ic_baseline_all_inbox_24)
        holder.navTitle.text = "タスク一覧"

        holder.view.setBackgroundColor(Color.LTGRAY)
    }

    override fun getItemCount(): Int {
        return 1
    }
}