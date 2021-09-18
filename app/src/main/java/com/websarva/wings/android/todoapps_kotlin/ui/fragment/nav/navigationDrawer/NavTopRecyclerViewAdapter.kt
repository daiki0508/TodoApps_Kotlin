package com.websarva.wings.android.todoapps_kotlin.ui.fragment.nav.navigationDrawer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.ui.OnItemClickListener

class NavTopRecyclerViewAdapter(
    private var type: Int,
    private var flag: Boolean,
    private var activity: FragmentActivity
): RecyclerView.Adapter<NavTopRecyclerViewHolder>() {
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavTopRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.row_nav_all_config, parent, false)

        return NavTopRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: NavTopRecyclerViewHolder, position: Int) {
        // theme情報を取得
        val preference = PreferenceManager.getDefaultSharedPreferences(activity)
        val themeId = preference.getString("theme", "0")

        // 0がタスク一覧
        if (type == 0){
            holder.navIcon.setImageResource(R.drawable.ic_baseline_all_inbox_24)
            holder.navTitle.apply {
                text = "タスク一覧"
                textSize = 25F
            }

            // trueがtodoActivity
            if (flag){
                holder.view.setBackgroundColor(Color.LTGRAY)
            }else{
                // nightModeかlightModeかで処理をわける
                if (themeId == "0"){
                    holder.view.setBackgroundColor(Color.WHITE)
                }else{
                    holder.view.setBackgroundColor(Color.parseColor("#696969"))
                }
                holder.view.setOnClickListener {
                    listener.onItemClickListener(it, position, list = null)
                }
            }
        }else{
            val mlp = holder.view.layoutParams as ViewGroup.MarginLayoutParams
            mlp.setMargins(mlp.leftMargin, 50, mlp.rightMargin, mlp.bottomMargin)

            holder.navIcon.setImageResource(R.drawable.ic_baseline_settings_24)
            holder.navTitle.apply {
                text = "設定"
                textSize = 20F
            }

            holder.view.setOnClickListener {
                listener.onItemClickListener(it, position, list = null)
            }
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }
}