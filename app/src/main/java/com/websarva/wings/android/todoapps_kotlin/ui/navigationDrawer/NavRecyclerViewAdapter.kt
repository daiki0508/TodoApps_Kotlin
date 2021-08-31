package com.websarva.wings.android.todoapps_kotlin.ui.navigationDrawer

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.ui.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.todo.recyclerView.RecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.viewModel.AddTodoTaskViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.TodoViewModel

class NavRecyclerViewAdapter(
    private var items: MutableList<MutableMap<String, String>>,
    private var position: Int,
    private var todoViewModel: TodoViewModel?,
    private var addTodoTaskViewModel: AddTodoTaskViewModel?,
    private var activity: Activity
): RecyclerView.Adapter<NavRecyclerViewHolder>() {
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.row_nav, parent, false)

        return NavRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: NavRecyclerViewHolder, position: Int) {
        // theme情報を取得
        val preference = PreferenceManager.getDefaultSharedPreferences(activity)
        val themeId = preference.getString("theme", "0")

        holder.title.text = items[position][FileName().list]

        if (todoViewModel != null) {
            holder.count.text =
                todoViewModel!!.countUnCompleteTask(items[position][FileName().list]!!).toString()
        } else {
            holder.count.text = addTodoTaskViewModel!!.countUnCompleteTask(
                items = null,
                items[position][FileName().list]
            ).toString()
        }

        if (position == this.position) {
            holder.view.setBackgroundColor(Color.LTGRAY)
        } else {
            // nightModeかlightModeかで処理をわける
            if (themeId == "0"){
                holder.view.setBackgroundColor(Color.WHITE)
            }else{
                holder.view.setBackgroundColor(Color.parseColor("#696969"))
            }
        }

        holder.view.setOnClickListener {
            listener.onItemClickListener(it, position, items[position][FileName().list]!!)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    fun getRecyclerViewSimpleCallBack(todoRecyclerView: RecyclerViewAdapter?) = object: ItemTouchHelper.SimpleCallback(
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

            if (todoViewModel != null){
                todoViewModel!!.move(items, fromPosition, toPosition)
                todoRecyclerView?.notifyItemMoved(fromPosition, toPosition)
            }else{
                addTodoTaskViewModel!!.move(items, fromPosition, toPosition, flag = true)
            }

            items.add(toPosition, items.removeAt(fromPosition))
            this@NavRecyclerViewAdapter.notifyItemMoved(fromPosition, toPosition)

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            return
        }
    }
}