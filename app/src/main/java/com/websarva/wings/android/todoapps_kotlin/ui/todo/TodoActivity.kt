package com.websarva.wings.android.todoapps_kotlin.ui.todo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.ui.DialogListener
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityTodoBinding
import com.websarva.wings.android.todoapps_kotlin.model.DownloadStatus
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.ui.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.add.AddTodoTaskActivity
import com.websarva.wings.android.todoapps_kotlin.ui.NetWorkFailureDialog
import com.websarva.wings.android.todoapps_kotlin.ui.navigationDrawer.NavRecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.ui.navigationDrawer.NavTopRecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.ui.settings.SettingsActivity
import com.websarva.wings.android.todoapps_kotlin.ui.todo.recyclerView.RecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.viewModel.TodoViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class TodoActivity : AppCompatActivity(), DialogListener {
    private lateinit var binding: ActivityTodoBinding
    private val viewModel: TodoViewModel by viewModel()

    private var networkStatus: Boolean? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var apAdapter: RecyclerViewAdapter? = null
    private var nvAdapter: NavRecyclerViewAdapter? = null
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTodoBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout,binding.toolbar, R.string.drawer_open, R.string.drawer_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // MainActivityからのintent情報を取得
        networkStatus = intent.getBooleanExtra("network", false)
        Log.d("network", networkStatus.toString())

        // ネットワークの接続状態によって処理を分岐
        if (networkStatus == true){
            auth = Firebase.auth
            storage = FirebaseStorage.getInstance()
            viewModel.setInit(auth, this, storage, networkStatus!!)
            viewModel.deleteAll()
        }else{
            viewModel.setInit(auth = null, this, storage = null, networkStatus!!)
        }

        // タスク一覧
        val nvTopAdapter = NavTopRecyclerViewAdapter(type = 0, flag = true)
        binding.navTopRecyclerView.adapter = nvTopAdapter
        binding.navTopRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.navTopRecyclerView.layoutManager = LinearLayoutManager(this)

        // 設定
        val nvSettingsAdapter = NavTopRecyclerViewAdapter(type = 1, flag = true)
        binding.navFooterRecyclerView.adapter = nvSettingsAdapter
        binding.navFooterRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.navFooterRecyclerView.layoutManager = LinearLayoutManager(this)

        nvSettingsAdapter.setOnItemClickListener(object: OnItemClickListener{
            override fun onItemClickListener(view: View, position: Int, list: String?) {
                Intent(this@TodoActivity, SettingsActivity::class.java).apply {
                    this.putExtra("flag", true)
                    this.putExtra("network", networkStatus)
                    startActivity(this)
                    finish()
                }
            }
        })

        // メイン画面
        binding.recyclerview.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.navRecyclerView.layoutManager = LinearLayoutManager(this)

        // ネットワークに接続されている場合のみ、FirebaseStoreからデータをダウンロード
        if (networkStatus == true){
            // TodoActivityで端末がoffline状態になった時の対応
            if (viewModel.connectingStatus() != null){
                viewModel.download(flag = true)
            }else{
                networkStatus = false
                if (File(filesDir, FileName().list).length() != 0L){
                    NetWorkFailureDialog(flag = false).show(supportFragmentManager, "NetWorkFailureDialog")
                    viewModel.createView()
                }
            }
        }else{
            if (File(filesDir, FileName().list).length() != 0L){
                viewModel.createView()
            }
        }

        viewModel.completeFlag().observe(this, {
            when {
                (it[DownloadStatus().list] == true) and (it[DownloadStatus().iv_aes_list] == true) and (it[DownloadStatus().salt_list] == true) and (it[DownloadStatus().task] == true) and (it[DownloadStatus().iv_aes_task] == true) and (it[DownloadStatus().salt_task] == true) -> {
                    viewModel.createView()
                }
                (it[DownloadStatus().list] == true) and (it[DownloadStatus().iv_aes_list] == true) and (it[DownloadStatus().salt_list] == true) and (it[DownloadStatus().task] == false) and (it[DownloadStatus().iv_aes_task] == false) and (it[DownloadStatus().salt_task] == false) -> {
                    viewModel.createView()
                }
                (it[DownloadStatus().list] == true) and (it[DownloadStatus().iv_aes_list] == true) and (it[DownloadStatus().salt_list] == true) -> {
                    viewModel.download(flag = false)
                }
            }
        })

        binding.fab.setOnClickListener {
            AddListDialog(flag = false, type = 0, position = null).show(supportFragmentManager, "AddListDialog")
        }

        viewModel.todoList().observe(this, {
            if (it.isNotEmpty() && apAdapter == null){
                binding.tvNoContent.visibility = View.GONE
                binding.recyclerview.visibility = View.VISIBLE

                // メイン画面
                apAdapter = RecyclerViewAdapter(it, this, viewModel)
                binding.recyclerview.adapter = apAdapter

                apAdapter!!.setOnItemClickListener(object: OnItemClickListener {
                    override fun onItemClickListener(view: View, position: Int, list: String?) {
                        addTodoIntent(list!!, position)
                    }
                })
                Log.d("test", "Called")

                // NavigationDrawer
                nvAdapter = NavRecyclerViewAdapter(it, -1, todoViewModel = viewModel, addTodoTaskViewModel = null)
                binding.navRecyclerView.adapter = nvAdapter
                itemTouchHelper = ItemTouchHelper(nvAdapter!!.getRecyclerViewSimpleCallBack(apAdapter))
                itemTouchHelper.attachToRecyclerView(binding.navRecyclerView)

                nvAdapter!!.setOnItemClickListener(object: OnItemClickListener{
                    override fun onItemClickListener(view: View, position: Int, list: String?) {
                        addTodoIntent(list!!, position)
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
        position: Int?
    ) {
        viewModel.update(list)

        if (apAdapter == null){
            viewModel.createView()
        }else{
            apAdapter!!.items.add(mutableMapOf(FileName().list to list))
            apAdapter?.notifyItemInserted(apAdapter!!.itemCount - 1)

            nvAdapter?.notifyItemInserted(nvAdapter!!.itemCount - 1)
        }
        // ネットワークに接続されている場合のみ、FirebaseStoreからデータをダウンロード
        if (networkStatus == true){
            // TodoActivityで端末がoffline状態になった時の対応
            if (viewModel.connectingStatus() != null){
                viewModel.upload()
            }else{
                networkStatus = false
                NetWorkFailureDialog(flag = false).show(supportFragmentManager, "NetWorkFailureDialog")
            }
        }
    }

    override fun onDialogReceive(flag: Boolean) {
        return
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        var retValue = true

        when(item.itemId){
            1 -> {
                val position = apAdapter!!.getPosition()
                Log.d("context", position.toString())

                val list = apAdapter!!.items[position][FileName().list]
                viewModel.deletePreference(list!!)

                apAdapter!!.items.removeAt(position)
                apAdapter?.notifyItemRemoved(position)

                nvAdapter?.notifyItemRemoved(position)

                if (apAdapter!!.itemCount == 0){
                    binding.recyclerview.visibility = View.GONE
                    binding.tvNoContent.visibility = View.VISIBLE

                    apAdapter = null
                    /*
                    ネットワークに接続されている場合のみ、FirebaseStoreからデータを削除
                     FirebaseStorageからlistとtaskを完全削除
                     trueがlistでfalseがtask
                     */
                    if (networkStatus == true){
                        // TodoActivityで端末がoffline状態になった時の対応
                        if (viewModel.connectingStatus() != null){
                            viewModel.delete(position, flag = true)
                            viewModel.delete(position, flag = false)
                        }else{
                            networkStatus = false
                            NetWorkFailureDialog(flag = false).show(supportFragmentManager, "NetWorkFailureDialog")
                        }
                    }
                    // 内部ストレージからtaskファイルを完全削除する
                    viewModel.listDelete(position)
                }else{
                    /*
                     内部ストレージのlistから該当task名を削除
                     該当taskも削除
                     ネットワークに接続されている場合のみ、FirebaseStoreからデータを削除
                     */
                    if (networkStatus == true){
                        // TodoActivityで端末がoffline状態になった時の対応
                        if (viewModel.connectingStatus() != null){
                            viewModel.delete(position, flag = false)
                        }else{
                            networkStatus = false
                            NetWorkFailureDialog(flag = false).show(supportFragmentManager, "NetWorkFailureDialog")
                        }
                    }
                    viewModel.listDelete(position)
                    // ネットワークに接続されている場合のみ、FirebaseStorageのlist更新
                    if (networkStatus == true){
                        // TodoActivityで端末がoffline状態になった時の対応
                        if (viewModel.connectingStatus() != null){
                            viewModel.upload()
                        }else{
                            networkStatus = false
                            NetWorkFailureDialog(flag = false).show(supportFragmentManager, "NetWorkFailureDialog")
                        }
                    }
                }
            }else -> retValue = super.onContextItemSelected(item)
        }
        return retValue
    }

    fun addTodoIntent(list: String, position: Int){
        Intent(this@TodoActivity, AddTodoTaskActivity::class.java).apply {
            this.putExtra(FileName().list, list)
            this.putExtra("position", position)
            this.putExtra("network", networkStatus)
            startActivity(this)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right,)
            finish()
        }
    }
}