package com.websarva.wings.android.todoapps_kotlin.ui.todo.recyclerView

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView.ChildRecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.viewModel.AddTodoTaskViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.TodoViewModel

class NavRecyclerViewAdapter(
    var items: MutableList<MutableMap<String, String>>,
    private var position: Int,
    private var todoViewModel: TodoViewModel?,
    private var addTodoTaskViewModel: AddTodoTaskViewModel?
): RecyclerView.Adapter<NavRecyclerViewHolder>() {
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.row_nav, parent, false)

        return NavRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: NavRecyclerViewHolder, position: Int) {
        holder.title.text = items[position]["list"]

        if (todoViewModel != null){
            holder.count.text = todoViewModel!!.countUnCompleteTask(items[position]["list"]!!).toString()
        }else{
            holder.count.text = addTodoTaskViewModel!!.countUnCompleteTask(items = null, items[position]["list"]).toString()
        }

        if (position == this.position){
            holder.view.setBackgroundColor(Color.LTGRAY)
        }else{
            holder.view.setBackgroundColor(Color.WHITE)
        }

        holder.view.setOnClickListener {
            listener.onItemClickListener(it, position, items[position]["list"]!!)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }
}