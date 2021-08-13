package com.websarva.wings.android.todoapps_kotlin.ui.todo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.DialogListener
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityTodoBinding

class TodoActivity : AppCompatActivity(), DialogListener {
    private lateinit var binding: ActivityTodoBinding

    private lateinit var auth: FirebaseAuth

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

        binding.fab.setOnClickListener {
            AddListDialog().show(supportFragmentManager, "AddListDialog")
        }
    }

    override fun onDialogFlagReceive(dialog: DialogFragment, list: String) {
        //TODO("Firebaseとの通信処理が未実装のため")
        //CryptClass().encrypt(this, "${auth.currentUser!!.uid}0000".toCharArray(), list)
        CryptClass().decrypt(this, "${auth.currentUser!!.uid}0000".toCharArray(), list)
    }
}