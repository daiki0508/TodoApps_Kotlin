package com.websarva.wings.android.todoapps_kotlin.ui.fragment.add.recyclerView

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.OnChildItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.OnPreferenceListener
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.add.AddTodoTaskViewModel

class ChildRecyclerViewAdapter(
    var items: MutableList<MutableMap<String, String>>,
    var viewModel: AddTodoTaskViewModel
    ): RecyclerView.Adapter<ChildRecyclerViewHolder>() {
    private lateinit var cListener: OnChildItemClickListener
    private lateinit var pListener: OnPreferenceListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view =  layoutInflater.inflate(R.layout.row_child_top, parent, false)

        return ChildRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildRecyclerViewHolder, position: Int) {
        val mlp = holder.content.layoutParams as ViewGroup.MarginLayoutParams
        mlp.setMargins(mlp.leftMargin, 15, mlp.rightMargin, 15)

        holder.content.apply {
            textSize = 22F
            isSingleLine = false
            text = items[position][FileName().task]

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

        // taskタップ時
        holder.content.setOnClickListener {
            cListener.onItemClickListener(it, position)
        }

        // taskが長押しされた時
        holder.content.setOnLongClickListener(View.OnLongClickListener {
            setPosition(holder.absoluteAdapterPosition)
            return@OnLongClickListener false
        })

        // contextMenuの生成Listener
        holder.contentView.setOnCreateContextMenuListener(holder)

        // checkBoxタップ時
        holder.checkBox.setOnClickListener {
            holder.content.apply {
                if (holder.checkBox.isChecked){
                    pListener.onPreferenceWriteListener(position, holder.content.text.toString(), true)
                }else{
                    pListener.onPreferenceWriteListener(position, holder.content.text.toString(), false)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private var position = 0
    fun getPosition(): Int{
        return position
    }

    private fun setPosition(position: Int){
        this.position = position
    }

    fun setOnItemClickListener(listener: OnChildItemClickListener){
        this.cListener = listener
    }

    fun setPreferenceListener(listener: OnPreferenceListener){
        this.pListener = listener
    }

    fun getRecyclerViewSimpleCallBack() = object: ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.ACTION_STATE_IDLE
    ){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.absoluteAdapterPosition
            val toPosition = target.absoluteAdapterPosition

            viewModel.move(items, fromPosition, toPosition, flag = false)

            items.add(toPosition, items.removeAt(fromPosition))
            this@ChildRecyclerViewAdapter.notifyItemMoved(fromPosition, toPosition)

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            return
        }
    }
}