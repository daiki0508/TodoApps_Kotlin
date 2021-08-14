package com.websarva.wings.android.todoapps_kotlin.ui.todo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.DialogListener
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityTodoBinding
import com.websarva.wings.android.todoapps_kotlin.ui.add.AddTodoListActivity
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

        //binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        viewModel.download(this, storage, auth)
        if (File(filesDir, "list").exists()){
            viewModel.createView(this, auth)
        }

        binding.fab.setOnClickListener {
            AddListDialog().show(supportFragmentManager, "AddListDialog")
        }

        viewModel.todoList().observe(this, {
            if (it.isNotEmpty()){
                binding.tvNoContent.visibility = View.GONE

                val adapter = RecyclerViewAdapter(it, this, viewModel)
                binding.recyclerview.adapter = adapter
                adapter.notifyDataSetChanged()

                adapter.setOnItemClickListener(object: OnItemClickListener{
                    override fun onItemClickListener(view: View, position: Int, list: String) {
                        addTodoIntent(list)
                    }
                })
                Log.d("test", "Called")
            }
        })
    }

    override fun onDialogFlagReceive(dialog: DialogFragment, list: String) {
        CryptClass().decrypt(this, "${auth.currentUser!!.uid}0000".toCharArray(), list, true)
        viewModel.upload(this, storage, auth)
        viewModel.createView(this, auth)
    }

    fun addTodoIntent(list: String){
        Intent(this@TodoActivity, AddTodoListActivity::class.java).apply {
            this.putExtra("list", list)
            startActivity(this)
        }
    }
}