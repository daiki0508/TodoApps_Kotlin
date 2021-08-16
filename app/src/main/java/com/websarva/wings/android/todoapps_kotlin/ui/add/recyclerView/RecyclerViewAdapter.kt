package com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.ui.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.add.AddTodoTaskActivity
import com.websarva.wings.android.todoapps_kotlin.viewModel.AddTodoTaskViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.TodoViewModel

interface OnItemClickListener {
    fun onItemClickListener(view: View, position: Int)
}

class RecyclerViewAdapter(
    var items: MutableList<MutableMap<String, String>>,
    var task: String,
    private var activity: AddTodoTaskActivity,
    private var viewModel: AddTodoTaskViewModel
    ): RecyclerView.Adapter<RecyclerViewHolder>() {
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.row_top, parent, false)

        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.title.textSize = 30F
        holder.title.text = task

        holder.title.setOnClickListener {
            listener.onItemClickListener(it, position)
        }

        holder.rvContents.layoutManager = LinearLayoutManager(activity)
        val adapter =  ChildRecyclerViewAdapter(items)
        holder.rvContents.adapter = adapter
        holder.rvContents.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        adapter.notifyDataSetChanged()

        adapter.setOnItemClickListener(object: OnChildItemClickListener{
            override fun onItemClickListener(view: View, position: Int) {
                // taskの更新
                viewModel.setPosition(position, items.size)
                AddListDialog(flag = true, type = 1, adapter, APAdapter = null, position).show(activity.supportFragmentManager, "UpdateTaskDialog")
            }
        })
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    override fun getItemCount(): Int {
        return 1
    }
}