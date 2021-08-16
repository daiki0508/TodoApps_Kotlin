package com.websarva.wings.android.todoapps_kotlin.ui.add

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.DialogListener
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityAddTodoListBinding
import com.websarva.wings.android.todoapps_kotlin.ui.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView.ChildRecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView.RecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.ui.todo.TodoActivity
import com.websarva.wings.android.todoapps_kotlin.viewModel.AddTodoTaskViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class AddTodoTaskActivity : AppCompatActivity(), DialogListener {
    private lateinit var binding: ActivityAddTodoListBinding
    private val viewModel: AddTodoTaskViewModel by viewModel()

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var task: String
    private var position = 0
    private var last = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddTodoListBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        auth = Firebase.auth
        storage = FirebaseStorage.getInstance()

        task = intent.getStringExtra("list")!!
        position = intent.getIntExtra("position", 0)
        last = intent.getIntExtra("last", 0)
        Log.d("intent", "$task: $position, $last")

        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        //viewModel.download(this, storage, auth, task)
        if (File("$filesDir/task/$task/task").exists()){
            viewModel.createView(this, auth, task)
        }

        binding.fab.setOnClickListener {
            AddListDialog(flag = true, type = 0, ACAdapter = null, APAdapter = null, position = null).show(supportFragmentManager, "AddTaskDialog")
        }

        var adapter: RecyclerViewAdapter? = null

        viewModel.todoTask().observe(this, {
            if (it.isNotEmpty()){
                binding.tvNoContent.visibility = View.GONE
                if (adapter == null){
                    adapter = RecyclerViewAdapter(viewModel.todoTask().value!!, task, this, viewModel)
                    binding.recyclerview.adapter = adapter
                    adapter?.notifyDataSetChanged()
                }

                adapter?.setOnItemClickListener(object: OnItemClickListener {
                    override fun onItemClickListener(view: View, position: Int) {
                        // listの更新
                        viewModel.setPosition(this@AddTodoTaskActivity.position, last)
                        AddListDialog(flag = false, type = 1, ACAdapter = null, APAdapter = adapter, position = null).show(supportFragmentManager, "UpdateListDialog")
                    }
                })
            }
        })
    }

    override fun onDialogFlagReceive(
        dialog: DialogFragment,
        list: String,
        type: Int,
        flag: Boolean,
        ACAdapter: ChildRecyclerViewAdapter?,
        APAdapter: RecyclerViewAdapter?,
        position: Int?
    ) {
        Log.d("dialog", list)
        if (type == 0){
            CryptClass().decrypt(this, "${auth.currentUser!!.uid}0000".toCharArray(), list, type = 1, task, aStr = null, flag = true)
            viewModel.createView(this, auth, task)
        }else{
            if (flag){
                viewModel.update(this, auth, task, list, flag)
            }else{
                viewModel.update(this, auth, task, list, flag)
            }
        }
        // trueがタスク、falseがリスト
        if (flag){
            viewModel.upload(this, storage, auth, task, flag)
            //viewModel.setTaskName(list)
            if (type == 1){
                ACAdapter!!.items[position!!]["task"] = list
                ACAdapter.notifyItemChanged(position)
            }
        }else{
            viewModel.upload(this, storage, auth, task = null, flag)
            viewModel.upload(this, storage, auth, task, flag = true)
            viewModel.delete(storage, auth, task)
            //viewModel.setListName(list)
            //viewModel.createView(this, auth, list)
            APAdapter!!.task = list
            APAdapter.notifyDataSetChanged()
            task = list
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, TodoActivity::class.java))
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}