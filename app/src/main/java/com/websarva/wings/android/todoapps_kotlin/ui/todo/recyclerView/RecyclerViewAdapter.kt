package com.websarva.wings.android.todoapps_kotlin.ui.todo.recyclerView

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.ui.todo.TodoActivity
import com.websarva.wings.android.todoapps_kotlin.viewModel.TodoViewModel

interface OnItemClickListener {
    fun onItemClickListener(view: View, position: Int, list: String)
}

class RecyclerViewAdapter(
    private var items: MutableList<MutableMap<String, String>>,
    private var activity: TodoActivity,
    private var viewModel: TodoViewModel,
    private var auth: FirebaseAuth
    ): RecyclerView.Adapter<RecyclerViewHolder>() {
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.row_top, parent, false)

        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.title.text = items[position]["list"]

        holder.view.setOnClickListener {
            listener.onItemClickListener(it, position, items[position]["list"]!!)
        }

        holder.rvContents.layoutManager = LinearLayoutManager(activity)
        val adapter =  ChildRecyclerViewAdapter(viewModel.getTask(activity, auth, items[position]["list"]!!), position)
        holder.rvContents.adapter = adapter
        adapter.notifyDataSetChanged()

        adapter.setOnItemClickListener(object: OnChildItemClickListener{
            override fun onItemClickListener(view: View, position: Int) {
                activity.addTodoIntent(items[position]["list"]!!, position, items.size)
            }
        })
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    override fun getItemCount(): Int {
        return items.size
    }
}