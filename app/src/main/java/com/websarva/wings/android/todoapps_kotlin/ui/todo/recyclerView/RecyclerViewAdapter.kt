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
import com.websarva.wings.android.todoapps_kotlin.ui.OnChildItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.OnPreferenceListener
import com.websarva.wings.android.todoapps_kotlin.ui.todo.TodoActivity
import com.websarva.wings.android.todoapps_kotlin.viewModel.TodoViewModel

class RecyclerViewAdapter(
    var items: MutableList<MutableMap<String, String>>,
    private var activity: TodoActivity,
    private var viewModel: TodoViewModel
    ): RecyclerView.Adapter<RecyclerViewHolder>() {
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.row_top, parent, false)

        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.title.text = items[position]["list"]

        holder.titleView.setOnClickListener {
            listener.onItemClickListener(it, position, items[position]["list"]!!)
        }

        holder.rvContents.layoutManager = LinearLayoutManager(activity)
        val adapter =  ChildRecyclerViewAdapter(viewModel.getTask(items[position]["list"]!!), this, position)
        holder.rvContents.adapter = adapter

        holder.titleView.setOnCreateContextMenuListener(holder)
        // listが長押しされた時
        holder.titleView.setOnLongClickListener(View.OnLongClickListener {
            setPosition(holder.absoluteAdapterPosition)
            return@OnLongClickListener false
        })

        adapter.setOnItemClickListener(object: OnChildItemClickListener{
            override fun onItemClickListener(view: View, position: Int) {
                activity.addTodoIntent(items[position]["list"]!!, position)
            }
        })

        adapter.setPreferenceListener(object: OnPreferenceListener{
            override fun onPreferenceWriteListener(
                position: Int,
                keyName: String,
                checkFlag: Boolean
            ) {
                return
            }

            override fun onPreferenceReadListener(keyName: String): Boolean {
                return viewModel.readPreference(holder.title.text.toString(), keyName)
            }
        })
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private var position = 0
    fun getPosition(): Int{
        return position
    }

    fun setPosition(position: Int){
        this.position = position
    }
}