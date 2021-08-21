package com.websarva.wings.android.todoapps_kotlin.ui.todo.recyclerView

import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.ui.OnChildItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.OnPreferenceListener

class ChildRecyclerViewAdapter(
    private var items: MutableList<MutableMap<String, String>>,
    private var apAdapter: RecyclerViewAdapter,
    private var position: Int
    ): RecyclerView.Adapter<ChildRecyclerViewHolder>() {
    private lateinit var listener: OnChildItemClickListener
    private lateinit var pListener: OnPreferenceListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view =  layoutInflater.inflate(R.layout.row_child_top, parent, false)

        return ChildRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildRecyclerViewHolder, position: Int) {
        holder.content.apply {
            ellipsize = TextUtils.TruncateAt.END
            text = items[position]["task"]

            if (pListener.onPreferenceReadListener(holder.content.text.toString())){
                setTextColor(Color.LTGRAY)
                paint.flags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                paint.isAntiAlias = true
                holder.checkBox.isChecked = true
            }else{
                setTextColor(Color.GRAY)
                paint.flags = Paint.ANTI_ALIAS_FLAG
                paint.isAntiAlias = false
                holder.checkBox.isChecked = false
            }
        }

        holder.checkBox.visibility = View.GONE

        holder.contentView.setOnCreateContextMenuListener(holder)
        holder.contentView.setOnLongClickListener(View.OnLongClickListener {
            apAdapter.setPosition(this.position)
            return@OnLongClickListener false
        })

        holder.contentView.setOnClickListener {
            listener.onItemClickListener(it, this.position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setOnItemClickListener(listener: OnChildItemClickListener){
        this.listener = listener
    }

    fun setPreferenceListener(listener: OnPreferenceListener){
        this.pListener = listener
    }
}