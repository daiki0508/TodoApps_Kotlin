package com.websarva.wings.android.todoapps_kotlin.ui.add

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.ui.DialogListener
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityAddTodoListBinding
import com.websarva.wings.android.todoapps_kotlin.ui.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.OnPreferenceListener
import com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView.*
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
    private var apAdapter: RecyclerViewAdapter? = null
    private var acAdapter: ChildRecyclerViewAdapter? = null

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

        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        /*
         FirebaseStorageからデータをダウンロード
         FirebaseStorageの料金タスクを抑えるために開発時は基本、内部ストレージのtaskファイルを利用
         */
        //viewModel.download(this, storage, auth, task, flag = true)
        if (File(filesDir, "task/$task/task").exists()){
            viewModel.createView(this, auth, task)
        }

        viewModel.completeFlag().observe(this, {
            // 全てのダウンロードが終了してからRecyclerViewの生成に入る
            if (it["task"]!! and it["iv_aes"]!! and it["salt"]!!){
                viewModel.createView(this, auth, task)
            }
        })

        viewModel.todoTask().observe(this, {
            if (it.isNotEmpty()){
                if (apAdapter == null){
                    // recyclerviewがdeleteTask等によって非表示の場合は再表示し、NoContentsを非表示にする
                    binding.tvNoContent.visibility = View.GONE
                    binding.recyclerview.visibility = View.VISIBLE

                    acAdapter = ChildRecyclerViewAdapter(it, viewModel)
                    apAdapter = RecyclerViewAdapter(viewModel.todoTask().value!!, task, this, viewModel, acAdapter)
                    binding.recyclerview.adapter = apAdapter

                    apAdapter?.setOnItemClickListener(object: OnItemClickListener {
                        override fun onItemClickListener(view: View, position: Int) {
                            // listの更新
                            viewModel.setPosition(this@AddTodoTaskActivity.position)
                            AddListDialog(flag = false, type = 1, position = position).show(supportFragmentManager, "UpdateListDialog")
                        }
                    })

                    acAdapter?.setPreferenceListener(object: OnPreferenceListener {
                        override fun onPreferenceWriteListener(position: Int, keyName: String, checkFlag: Boolean) {
                            // checkBoxの状態を保存しUIに反映
                            viewModel.writePreference(this@AddTodoTaskActivity, task, keyName, checkFlag)
                            acAdapter!!.notifyItemChanged(position)
                        }

                        override fun onPreferenceReadListener(keyName: String): Boolean {
                            return viewModel.readPreference(this@AddTodoTaskActivity, task, keyName)
                        }
                    })
                }
            }
        })

        binding.fab.setOnClickListener {
            AddListDialog(flag = true, type = 0, position = null).show(supportFragmentManager, "AddTaskDialog")
        }
    }

    override fun onDialogFlagReceive(
        dialog: DialogFragment,
        list: String,
        type: Int,
        flag: Boolean,
        position: Int?
    ) {
        Log.d("dialog", list)
        if (type == 0){
            CryptClass().decrypt(this, "${auth.currentUser!!.uid}0000".toCharArray(), list, type = 1, task, aStr = null, flag = true)
        }else{
            if (flag){
                viewModel.update(this, auth, task, list, flag)
            }else{
                viewModel.update(this, auth, task, list, flag)
            }
        }
        // trueがタスク、falseがリスト
        if (flag){
            //viewModel.upload(this, storage, auth, task, flag)
            when {
                // taskの更新
                type == 1 -> {
                    viewModel.writePreference(this, task, keyName = acAdapter!!.items[position!!]["task"]!!, checkFlag = false)
                    acAdapter!!.items[position]["task"] = list
                    acAdapter!!.notifyItemChanged(position)
                }
                acAdapter == null -> {
                    // 最初のtaskの追加
                    viewModel.createView(this, auth, task)
                }
                else -> {
                    // taskの追加
                    acAdapter!!.items.add(mutableMapOf("task" to list))
                    acAdapter!!.notifyItemInserted(acAdapter!!.itemCount - 1)
                }
            }
        }else{
            /*viewModel.upload(this, storage, auth, task = null, flag)
            viewModel.upload(this, storage, auth, list, flag = true)
            viewModel.delete(storage, auth, task)*/

            // preferenceを手動でmove
            for (i in 0 until acAdapter!!.items.size){
                viewModel.writePreference(
                    this,
                    task = list,
                    keyName = acAdapter!!.items[i]["task"]!!,
                    checkFlag = viewModel.readPreference(this, task, acAdapter!!.items[i]["task"]!!))
            }
            // 旧preferenceを削除
            viewModel.deletePreference(this, task)

            apAdapter!!.task = list
            apAdapter!!.notifyItemChanged(position!!)
            task = list
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_options_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var retValue = true
        when(item.itemId){
            R.id.allCheck -> {
                for (i in 0 until acAdapter!!.itemCount){
                    viewModel.writePreference(this, task, keyName = acAdapter!!.items[i]["task"]!!, checkFlag = true)
                }
                acAdapter!!.notifyDataSetChanged()
            }
            R.id.allUnCheck ->{
                for (i in 0 until acAdapter!!.itemCount){
                    viewModel.writePreference(this, task, keyName = acAdapter!!.items[i]["task"]!!, checkFlag = false)
                }
                acAdapter!!.notifyDataSetChanged()
            }
            else -> retValue = super.onOptionsItemSelected(item)
        }
        return retValue
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.d("context", "Called!")
        var retValue = true

        when(item.itemId){
            // taskの削除
            1 -> {
                val position = acAdapter!!.getPosition()
                Log.d("context", position.toString())
                acAdapter!!.items.removeAt(position)
                acAdapter!!.notifyItemRemoved(position)

                /*
                 taskの件数が0になったらrecyclerviewを非表示にしてNoContentを表示
                 acAdapterとapAdapterを初期化
                 */
                if (acAdapter!!.itemCount == 0){
                    binding.recyclerview.visibility = View.GONE
                    binding.tvNoContent.visibility = View.VISIBLE

                    acAdapter = null
                    apAdapter = null
                    // FirebaseStorageからtaskを完全削除
                    viewModel.delete(storage, auth, task)
                    // 内部ストレージからtaskファイルを完全削除する
                    viewModel.taskDelete(this, auth, task, position)
                }else{
                    // 内部ストレージから該当taskを削除
                    viewModel.taskDelete(this, auth, task, position)
                    // FirebaseStorageを更新
                    viewModel.upload(this, storage, auth, task, flag = true)
                }
            }
            else -> retValue = super.onContextItemSelected(item)
        }
        return retValue
    }

    override fun onBackPressed() {
        startActivity(Intent(this, TodoActivity::class.java))
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        finish()
    }
}