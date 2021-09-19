package com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.recyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.ui.OnChildItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.OnPreferenceListener
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.TodoFragment
import com.websarva.wings.android.todoapps_kotlin.viewModel.PrivateTodoViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.TodoViewModel

class RecyclerViewAdapter(
    var items: MutableList<MutableMap<String, String>>,
    private var fragment: TodoFragment,
    private var todoViewModel: PrivateTodoViewModel,
    private val viewModel: TodoViewModel
    ): RecyclerView.Adapter<RecyclerViewHolder>() {
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.row_top, parent, false)

        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.title.text = items[position][FileName().list]

        holder.titleView.setOnClickListener {
            listener.onItemClickListener(it, position, items[position][FileName().list]!!)
        }

        holder.rvContents.layoutManager = LinearLayoutManager(fragment.requireActivity())
        val adapter =  ChildRecyclerViewAdapter(todoViewModel.getTask(items[position][FileName().list]!!, viewModel), this, position)
        holder.rvContents.adapter = adapter

        holder.titleView.setOnCreateContextMenuListener(holder)
        // listが長押しされた時
        holder.titleView.setOnLongClickListener(View.OnLongClickListener {
            setPosition(holder.absoluteAdapterPosition)
            return@OnLongClickListener false
        })

        adapter.setOnItemClickListener(object: OnChildItemClickListener{
            override fun onItemClickListener(view: View, position: Int) {
                // TODO("未実装")
                todoViewModel.setBundle(items[position][FileName().list]!!, position)
                //fragment.addTodoIntent(items[position][FileName().list]!!, position)
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