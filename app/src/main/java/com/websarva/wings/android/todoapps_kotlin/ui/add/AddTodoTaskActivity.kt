package com.websarva.wings.android.todoapps_kotlin.ui.add

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.DialogListener
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityAddTodoListBinding
import com.websarva.wings.android.todoapps_kotlin.ui.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView.RecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.ui.todo.TodoActivity
import com.websarva.wings.android.todoapps_kotlin.ui.todo.recyclerView.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.viewModel.AddTodoTaskViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class AddTodoTaskActivity : AppCompatActivity(), DialogListener {
    private lateinit var binding: ActivityAddTodoListBinding
    private val viewModel: AddTodoTaskViewModel by viewModel()

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var task: String

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
        Log.d("intent", task)

        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        viewModel.download(this, storage, auth, task)
        if (File("$filesDir/task/$task/task").exists()){
            viewModel.createView(this, auth, task)
        }

        binding.fab.setOnClickListener {
            AddListDialog(flag = true).show(supportFragmentManager, "AddTaskFragment")
        }

        viewModel.todoTask().observe(this, {
            if (it.isNotEmpty()){
                binding.tvNoContent.visibility = View.GONE

                val adapter = RecyclerViewAdapter(it, task, this, viewModel)
                binding.recyclerview.adapter = adapter
                adapter.notifyDataSetChanged()

                Log.d("test", "Called")
            }
        })
    }

    override fun onDialogFlagReceive(dialog: DialogFragment, list: String) {
        Log.d("dialog", list)
        CryptClass().decrypt(this, "${auth.currentUser!!.uid}0000".toCharArray(), list, type = 1, task, flag = true)
        viewModel.upload(this, storage, auth, task)
        viewModel.createView(this, auth, task)
    }

    override fun onBackPressed() {
        startActivity(Intent(this, TodoActivity::class.java))
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}