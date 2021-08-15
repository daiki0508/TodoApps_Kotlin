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
import com.websarva.wings.android.todoapps_kotlin.ui.add.AddTodoTaskActivity
import com.websarva.wings.android.todoapps_kotlin.ui.todo.TodoActivity
import com.websarva.wings.android.todoapps_kotlin.viewModel.AddTodoTaskViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.TodoViewModel

interface OnItemClickListener {
    fun onItemClickListener(view: View, position: Int, list: String)
}

class RecyclerViewAdapter(
    private var items: MutableList<MutableMap<String, String>>,
    private var task: String,
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

        holder.view.setOnClickListener {
            TODO("未実装")
        }

        holder.rvContents.layoutManager = LinearLayoutManager(activity)
        /*val contentList: MutableList<MutableMap<String, String>> = mutableListOf()
        var content: MutableMap<String, String>
        // 動作確認用のテストリストの作成
        for (i in 0..2){
            content = mutableMapOf("content" to "test$i")
            contentList.add(content)
        }*/
        val adapter =  ChildRecyclerViewAdapter(items)
        holder.rvContents.adapter = adapter
        holder.rvContents.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        adapter.notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    override fun getItemCount(): Int {
        return 1
    }
}