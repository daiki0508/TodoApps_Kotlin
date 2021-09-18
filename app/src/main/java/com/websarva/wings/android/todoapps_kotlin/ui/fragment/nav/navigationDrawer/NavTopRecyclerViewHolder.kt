package com.websarva.wings.android.todoapps_kotlin.ui.fragment.nav.navigationDrawer

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R

class NavTopRecyclerViewHolder(var view: View): RecyclerView.ViewHolder(view) {
    val navIcon: ImageView = view.findViewById(R.id.navIcon)
    val navTitle: TextView = view.findViewById(R.id.navTitle)
}