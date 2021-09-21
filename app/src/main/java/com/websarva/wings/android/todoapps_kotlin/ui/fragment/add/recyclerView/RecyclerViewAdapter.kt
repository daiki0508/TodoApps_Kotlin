package com.websarva.wings.android.todoapps_kotlin.ui.fragment.add.recyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.OnChildItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.add.AddTodoTaskFragment
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.add.AddTodoTaskViewModel

class RecyclerViewAdapter(
    var itemTouchHelper: ItemTouchHelper,
    var task: String,
    private var fragment: AddTodoTaskFragment,
    private var viewModel: AddTodoTaskViewModel,
    var ACAdapter: ChildRecyclerViewAdapter?
    ): RecyclerView.Adapter<RecyclerViewHolder>() {
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.row_top, parent, false)

        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.title.apply {
            textSize = 30F
            text = task
            setOnClickListener {
                listener.onItemClickListener(it, position, list = null)
            }
        }

        fragment.activity?.let {
            holder.rvContents.layoutManager = LinearLayoutManager(it)
            holder.rvContents.adapter = ACAdapter
            holder.rvContents.addItemDecoration(DividerItemDecoration(it, DividerItemDecoration.VERTICAL))

            ACAdapter!!.setOnItemClickListener(object: OnChildItemClickListener {
                override fun onItemClickListener(view: View, position: Int) {
                    // taskの更新
                    viewModel.setPosition(position)
                    AddListDialog(flag = true, type = 1, position).show(it.supportFragmentManager, "UpdateTaskDialog")
                }
            })
        }

        itemTouchHelper.attachToRecyclerView(holder.rvContents)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    override fun getItemCount(): Int {
        return 1
    }
}