package com.websarva.wings.android.todoapps_kotlin.ui.add

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityAddTodoListBinding
import com.websarva.wings.android.todoapps_kotlin.ui.todo.AddListDialog

class AddTodoListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTodoListBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddTodoListBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        auth = Firebase.auth
        storage = FirebaseStorage.getInstance()

        val list = intent.getStringExtra("list")
        Log.d("intent", list!!)

        binding.fab.setOnClickListener {
            TODO("未実装")
        }
    }
}