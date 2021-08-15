package com.websarva.wings.android.todoapps_kotlin.ui.add

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.DialogListener
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityAddTodoListBinding
import com.websarva.wings.android.todoapps_kotlin.ui.AddListDialog

class AddTodoTaskActivity : AppCompatActivity(), DialogListener {
    private lateinit var binding: ActivityAddTodoListBinding

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

        binding.fab.setOnClickListener {
            AddListDialog(flag = true).show(supportFragmentManager, "AddTaskFragment")
        }
    }

    override fun onDialogFlagReceive(dialog: DialogFragment, list: String) {
        Log.d("dialog", list)
        //TODO("Firebaseとの通信処理が未実装のため")
        CryptClass().decrypt(this, "${auth.currentUser!!.uid}0000".toCharArray(), list, type = 1, task, flag = true)
    }
}