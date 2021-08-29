package com.websarva.wings.android.todoapps_kotlin.ui.add

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.ui.DialogListener
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityAddTodoListBinding
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.ui.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.OnPreferenceListener
import com.websarva.wings.android.todoapps_kotlin.ui.add.recyclerView.*
import com.websarva.wings.android.todoapps_kotlin.ui.NetWorkFailureDialog
import com.websarva.wings.android.todoapps_kotlin.ui.todo.TodoActivity
import com.websarva.wings.android.todoapps_kotlin.ui.navigationDrawer.NavRecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.ui.navigationDrawer.NavTopRecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.ui.settings.SettingsActivity
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
    private var networkStatus: Boolean? = null
    private var apAdapter: RecyclerViewAdapter? = null
    private var acAdapter: ChildRecyclerViewAdapter? = null
    private var nvAdapter: NavRecyclerViewAdapter? = null
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var nvItemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddTodoListBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout,binding.toolbar, R.string.drawer_open, R.string.drawer_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        task = intent.getStringExtra(FileName().list)!!
        position = intent.getIntExtra("position", 0)
        networkStatus = intent.getBooleanExtra("network", false)

        // ネットワークに接続されている場合はインスタンスを取得
        if (networkStatus == true){
            auth = Firebase.auth
            storage = FirebaseStorage.getInstance()

            viewModel.setInit(list = task, auth, this, storage, networkStatus!!)
        }else{
            viewModel.setInit(list = task, auth = null, this, storage = null, networkStatus!!)
        }

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.unCompleteCount.visibility = View.GONE
        binding.navRecyclerView.layoutManager = LinearLayoutManager(this)

        /*
         FirebaseStorageからデータをダウンロード
         FirebaseStorageの料金タスクを抑えるために開発時は基本、内部ストレージのtaskファイルを利用
         */
        //viewModel.download(this, storage, auth, task)
        if (File(filesDir, "task/$task/${FileName().task}").length() != 0L){
            viewModel.createView()
        }

        // タスク一覧
        val nvTopAdapter = NavTopRecyclerViewAdapter(type = 0, flag = false)
        binding.navTopRecyclerView.adapter = nvTopAdapter
        binding.navTopRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.navTopRecyclerView.layoutManager = LinearLayoutManager(this)

        nvTopAdapter.setOnItemClickListener(object: OnItemClickListener{
            override fun onItemClickListener(view: View, position: Int, list: String?) {
                todoIntent()
            }
        })

        // 設定
        val nvSettingsAdapter = NavTopRecyclerViewAdapter(type = 1, flag = true)
        binding.navFooterRecyclerView.adapter = nvSettingsAdapter
        binding.navFooterRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.navFooterRecyclerView.layoutManager = LinearLayoutManager(this)

        nvSettingsAdapter.setOnItemClickListener(object: OnItemClickListener{
            override fun onItemClickListener(view: View, position: Int, list: String?) {
                Intent(this@AddTodoTaskActivity, SettingsActivity::class.java).apply {
                    this.putExtra("flag", false)
                    this.putExtra(FileName().list, task)
                    this.putExtra("position", this@AddTodoTaskActivity.position)
                    this.putExtra("network", networkStatus)
                    startActivity(this)
                    finish()
                }
            }
        })

        // NavigationDrawer
        nvAdapter = NavRecyclerViewAdapter(viewModel.getList(), this.position, todoViewModel = null, addTodoTaskViewModel = viewModel)
        binding.navRecyclerView.adapter = nvAdapter
        nvItemTouchHelper = ItemTouchHelper(nvAdapter!!.getRecyclerViewSimpleCallBack(todoRecyclerView = null))
        nvItemTouchHelper.attachToRecyclerView(binding.navRecyclerView)

        nvAdapter!!.setOnItemClickListener(object: OnItemClickListener {
            override fun onItemClickListener(view: View, position: Int, list: String?) {
                Intent(intent).apply {
                    this.putExtra(FileName().list, list)
                    this.putExtra("position", position)
                    startActivity(this)
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    finish()
                }
            }
        })

        viewModel.todoTask().observe(this, {
            if (it.isNotEmpty()){
                if (apAdapter == null){
                    // recyclerviewがdeleteTask等によって非表示の場合は再表示し、NoContentsを非表示にする
                    binding.tvNoContent.visibility = View.GONE
                    binding.recyclerview.visibility = View.VISIBLE
                    binding.unCompleteCount.visibility = View.VISIBLE

                    acAdapter = ChildRecyclerViewAdapter(it, viewModel)
                    itemTouchHelper = ItemTouchHelper(acAdapter!!.getRecyclerViewSimpleCallBack())

                    apAdapter = RecyclerViewAdapter(itemTouchHelper, task, this, viewModel, acAdapter)
                    binding.recyclerview.adapter = apAdapter

                    apAdapter?.setOnItemClickListener(object: OnItemClickListener {
                        override fun onItemClickListener(view: View, position: Int, list: String?) {
                            // listの更新
                            viewModel.setPosition(this@AddTodoTaskActivity.position)
                            AddListDialog(flag = false, type = 1, position = position).show(supportFragmentManager, "UpdateListDialog")
                        }
                    })

                    acAdapter?.setPreferenceListener(object: OnPreferenceListener {
                        override fun onPreferenceWriteListener(position: Int, keyName: String, checkFlag: Boolean) {
                            // checkBoxの状態を保存しUIに反映
                            viewModel.writePreference(keyName, checkFlag)
                            acAdapter?.notifyItemChanged(position)

                            nvAdapter?.notifyItemChanged(position)
                        }

                        override fun onPreferenceReadListener(keyName: String): Boolean {
                            // 未完了タスクの件数をtextviewとNavigationDrawerに通知
                            binding.unCompleteCount.text = getString(R.string.unCompleteTaskCount, viewModel.countUnCompleteTask(acAdapter!!.items, list = null))
                            nvAdapter?.notifyItemChanged(this@AddTodoTaskActivity.position)

                            return viewModel.readPreference(keyName)
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
        //Log.d("dialog", list)
        if (type == 0){
            viewModel.add(list)
        }else{
            if (flag){
                viewModel.update(list, flag)
            }else{
                viewModel.update(list, flag)
            }
        }
        // trueがタスク、falseがリスト
        if (flag){
            when {
                // taskの更新
                type == 1 -> {
                    viewModel.writePreference(keyName = acAdapter!!.items[position!!][FileName().task]!!, checkFlag = false)
                    acAdapter!!.items[position][FileName().task] = list
                    acAdapter!!.notifyItemChanged(position)
                }
                acAdapter == null -> {
                    // 最初のtaskの追加
                    viewModel.createView()
                }
                else -> {
                    // taskの追加
                    acAdapter!!.items.add(mutableMapOf(FileName().task to list))
                    acAdapter!!.notifyItemInserted(acAdapter!!.itemCount - 1)
                }
            }
            // ネットワークに接続されている場合のみ、アップロード
            if (networkStatus == true){
                if (viewModel.connectingStatus() != null){
                    viewModel.upload(flag)
                }else{
                    networkStatus = false
                    NetWorkFailureDialog(flag = false).show(supportFragmentManager, "NetWorkFailureDialog")
                }
            }
        }else{
            // ネットワークに接続されている場合のみ、アップロードや削除を行う
            if (networkStatus == true){
                if (viewModel.connectingStatus() != null){
                    viewModel.upload(flag)
                    viewModel.upload(flag = true)
                    viewModel.delete()
                }else{
                    networkStatus = false
                    NetWorkFailureDialog(flag = false).show(supportFragmentManager, "NetWorkFailureDialog")
                }
            }

            // preferenceを手動でmove
            for (i in 0 until acAdapter!!.items.size){
                viewModel.writePreference(
                    keyName = acAdapter!!.items[i][FileName().task]!!,
                    checkFlag = viewModel.readPreference(acAdapter!!.items[i][FileName().task]!!))
            }
            // 旧preferenceを削除
            viewModel.deletePreference(task)

            apAdapter!!.task = list
            apAdapter!!.notifyItemChanged(position!!)
            task = list
        }
    }

    override fun onDialogReceive(flag: Boolean) {
        return
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
                    viewModel.writePreference(keyName = acAdapter!!.items[i][FileName().task]!!, checkFlag = true)
                    acAdapter?.notifyItemChanged(i)
                }
            }
            R.id.allUnCheck ->{
                for (i in 0 until acAdapter!!.itemCount){
                    viewModel.writePreference(keyName = acAdapter!!.items[i][FileName().task]!!, checkFlag = false)
                    acAdapter?.notifyItemChanged(i)
                }
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

                nvAdapter?.notifyItemChanged(this.position)

                /*
                 taskの件数が0になったらrecyclerviewを非表示にしてNoContentを表示
                 acAdapterとapAdapterを初期化
                 */
                if (acAdapter!!.itemCount == 0){
                    binding.recyclerview.visibility = View.GONE
                    binding.tvNoContent.visibility = View.VISIBLE
                    binding.unCompleteCount.visibility = View.GONE

                    acAdapter = null
                    apAdapter = null
                    /*
                     FirebaseStorageからtaskを完全削除
                     ネットワークに接続されている場合のみ、削除
                     */
                    if (networkStatus == true){
                        if (viewModel.connectingStatus() != null){
                            viewModel.delete()
                        }else{
                            networkStatus = false
                            NetWorkFailureDialog(flag = false).show(supportFragmentManager, "NetWorkFailureDialog")
                        }
                    }
                    // 内部ストレージからtaskファイルを完全削除する
                    viewModel.taskDelete(position)
                }else{
                    binding.unCompleteCount.text = getString(R.string.unCompleteTaskCount, viewModel.countUnCompleteTask(acAdapter!!.items, list = null))

                    // 内部ストレージから該当taskを削除
                    viewModel.taskDelete(position)
                    /*
                     FirebaseStorageを更新
                     ネットワークに接続されている場合のみ、アップロード
                     */
                    if (networkStatus == true){
                        if (viewModel.connectingStatus() != null){
                            viewModel.upload(flag = true)
                        }else{
                            networkStatus = false
                            NetWorkFailureDialog(flag = false).show(supportFragmentManager, "NetWorkFailureDialog")
                        }
                    }
                }
            }
            else -> retValue = super.onContextItemSelected(item)
        }
        return retValue
    }

    override fun onBackPressed() {
        todoIntent()
    }

    private fun todoIntent(){
        startActivity(Intent(this, TodoActivity::class.java))
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        finish()
    }
}