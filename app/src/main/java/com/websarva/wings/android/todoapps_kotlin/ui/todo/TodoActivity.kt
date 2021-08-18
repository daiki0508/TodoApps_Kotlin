package com.websarva.wings.android.todoapps_kotlin.ui.todo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.DialogListener
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityTodoBinding
import com.websarva.wings.android.todoapps_kotlin.ui.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.add.AddTodoTaskActivity
import com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView.ChildRecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.ui.todo.recyclerView.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.todo.recyclerView.RecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.viewModel.TodoViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class TodoActivity : AppCompatActivity(), DialogListener {
    private lateinit var binding: ActivityTodoBinding
    private val viewModel: TodoViewModel by viewModel()

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser == null){
            Log.w("test", "Error...")
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTodoBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        auth = Firebase.auth
        storage = FirebaseStorage.getInstance()

        binding.recyclerview.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        //viewModel.download(this, storage, auth)
        /*if (File(filesDir, "list").exists()){
            viewModel.createView(this, auth)
        }*/

        viewModel.completeFlag().observe(this, {
            // 全てのダウンロードが終了してからRecyclerViewの生成に入る
            if (it["task"]!! and it["iv_aes"]!! and it["salt"]!!){
                viewModel.createView(this, auth)
            }
        })

        binding.fab.setOnClickListener {
            AddListDialog(flag = false, type = 0, position = null).show(supportFragmentManager, "AddListDialog")
        }

        viewModel.todoList().observe(this, {
            if (it.isNotEmpty()){
                binding.tvNoContent.visibility = View.GONE

                val adapter = RecyclerViewAdapter(it, this, viewModel, auth)
                binding.recyclerview.adapter = adapter
                adapter.notifyDataSetChanged()

                adapter.setOnItemClickListener(object: OnItemClickListener{
                    override fun onItemClickListener(view: View, position: Int, list: String) {
                        addTodoIntent(list, position, it.size)
                    }
                })
                Log.d("test", "Called")
            }
        })
    }

    override fun onDialogFlagReceive(
        dialog: DialogFragment,
        list: String,
        type: Int,
        flag: Boolean,
        position: Int?
    ) {
        CryptClass().decrypt(this, "${auth.currentUser!!.uid}0000".toCharArray(), list, type = 0, task = null, aStr = null, flag = true)
        //viewModel.upload(this, storage, auth)
        //viewModel.createView(this, auth)
    }

    fun addTodoIntent(list: String, position: Int, last: Int){
        Intent(this@TodoActivity, AddTodoTaskActivity::class.java).apply {
            this.putExtra("list", list)
            this.putExtra("position", position)
            this.putExtra("last", last)
            startActivity(this)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right,)
            finish()
        }
    }
}