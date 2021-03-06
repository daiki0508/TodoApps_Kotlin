package com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.*
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.balloon.showAlignTop
import com.websarva.wings.android.todoapps_kotlin.BuildConfig
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.databinding.FragmentTodoBinding
import com.websarva.wings.android.todoapps_kotlin.model.DialogBundle
import com.websarva.wings.android.todoapps_kotlin.model.DownloadStatus
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.NetWorkFailureDialog
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.add.AddTodoTaskFragment
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.recyclerView.RecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.AfterLoginViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.nav.NavigationViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.todo.PrivateTodoViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.todo.TodoViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class TodoFragment : Fragment(){
    private var _binding: FragmentTodoBinding? = null
    private val binding
    get() = _binding!!

    private val viewModel: TodoViewModel by sharedViewModel()
    private val afterLoginViewModel by activityViewModels<AfterLoginViewModel>()
    private val privateViewModel: PrivateTodoViewModel by viewModel()
    private val navigationViewModel: NavigationViewModel by sharedViewModel()

    private var networkStatus: Boolean? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var apAdapter: RecyclerViewAdapter? = null

    private lateinit var transaction: FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AddListDialogからの結果を取得
        setFragmentResultListener(DialogBundle.Result.name){ _, data ->
            privateViewModel.setList(data.getString(DialogBundle.List.name, ""))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodoBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!BuildConfig.DEBUG){
            activity?.window!!.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }

        // MainActivityからのintent情報を取得
        networkStatus = afterLoginViewModel.networkStatus().value
        Log.d("network", networkStatus.toString())

        transaction = requireActivity().supportFragmentManager.beginTransaction()

        // ネットワークの接続状態によって処理を分岐
        if (networkStatus == true){
            auth = Firebase.auth
            storage = FirebaseStorage.getInstance()
            viewModel.setInit(auth, storage, networkStatus!!)
            privateViewModel.setInit(auth, storage, networkStatus!!)
            privateViewModel.deleteAll()
        }else{
            viewModel.setInit(auth = null, storage = null, networkStatus!!)
            privateViewModel.setInit(auth = null, storage = null, networkStatus!!)
        }

        privateViewModel.deleteAllComplete.observe(this.viewLifecycleOwner, { event ->
            event?.contentIfNotHandled.let { flag ->
                if (flag != null && flag){
                    // balloonの作成
                    privateViewModel.showBalloonFlag(this)
                }
            }
        })

        with(privateViewModel){
            contentBalloon1().observe(this@TodoFragment.viewLifecycleOwner, {
                // balloonの表示順番を設定
                fabBalloon().value!!.relayShowAlignBottom(contentBalloon0().value!!, binding.tvNoContent)
                    .relayShowAlignBottom(contentBalloon1().value!!, binding.tvNoContent)

                // balloonの表示
                binding.fab.showAlignTop(fabBalloon().value!!)

                it.setOnBalloonDismissListener {
                    // 実行したことを保存
                    save()
                    // balloonCompleteに通知
                    setBalloonComplete()
                }
            })

            // balloonCompleteのobserver
            balloonComplete().observe(this@TodoFragment.viewLifecycleOwner, { flag ->
                if (flag){
                    activity?.let {
                        // メイン画面
                        binding.recyclerview.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

                        // ネットワークに接続されている場合のみ、FirebaseStoreからデータをダウンロード
                        if (networkStatus == true){
                            // TodoActivityで端末がoffline状態になった時の対応
                            if (viewModel.connectingStatus() != null){
                                privateViewModel.download(flag = true, viewModel)
                            }else{
                                networkStatus = false
                                if (File(it.filesDir, FileName().list).length() != 0L){
                                    NetWorkFailureDialog(flag = false).show(it.supportFragmentManager, "NetWorkFailureDialog")
                                    viewModel.createView()
                                }
                            }
                        }else{
                            if (File(it.filesDir, FileName().list).length() != 0L){
                                viewModel.createView()
                            }
                        }
                    }
                }
            })
        }

        //completeFlagのobserver
        viewModel.completeFlag().observe(this.viewLifecycleOwner, {
            when {
                (it[DownloadStatus().list] == true) and (it[DownloadStatus().iv_aes_list] == true) and (it[DownloadStatus().salt_list] == true) and (it[DownloadStatus().task] == true) and (it[DownloadStatus().iv_aes_task] == true) and (it[DownloadStatus().salt_task] == true) -> {
                    viewModel.createView()
                }
                (it[DownloadStatus().list] == true) and (it[DownloadStatus().iv_aes_list] == true) and (it[DownloadStatus().salt_list] == true) and (it[DownloadStatus().task] == false) and (it[DownloadStatus().iv_aes_task] == false) and (it[DownloadStatus().salt_task] == false) -> {
                    viewModel.createView()
                }
                (it[DownloadStatus().list] == true) and (it[DownloadStatus().iv_aes_list] == true) and (it[DownloadStatus().salt_list] == true) -> {
                    privateViewModel.download(flag = false, viewModel)
                }
            }
        })

        binding.fab.setOnClickListener {
            AddListDialog(flag = false, type = 0, position = null).show(requireActivity().supportFragmentManager, "AddListDialog")
        }

        // todoListのobserver
        viewModel.todoList.observe(this.viewLifecycleOwner, { event ->
            event.contentIfNotHandled.let {
                if (it != null){
                    if (apAdapter == null){
                        binding.tvNoContent.visibility = View.GONE
                        binding.recyclerview.visibility = View.VISIBLE

                        // メイン画面
                        apAdapter = RecyclerViewAdapter(it, this, privateViewModel, viewModel)
                        binding.recyclerview.adapter = apAdapter
                        viewModel.setAdapter(apAdapter!!)

                        // navigationに通知
                        navigationViewModel.setFlag(flag = true)
                        navigationViewModel.setPosition(-1)

                        apAdapter!!.setOnItemClickListener(object: OnItemClickListener {
                            override fun onItemClickListener(view: View, position: Int, list: String?) {
                                privateViewModel.setBundle(list!!, position)
                            }
                        })
                        Log.d("test", "Called")
                    }
                }
            }
        })

        // bundleのobserver
        privateViewModel.bundle().observe(this.viewLifecycleOwner, {
            AddTodoTaskFragment().apply {
                // bundleに値をセット
                this.arguments = it

                // AddTodoTaskFragmentへ遷移
                transaction.setCustomAnimations(R.anim.nav_up_enter_anim, R.anim.nav_up_exit_anim)
                transaction.replace(R.id.container, this).commit()
            }
        })

        // listのobserver
        privateViewModel.list().observe(this.viewLifecycleOwner, {
            if (it.isNotEmpty()){
                privateViewModel.update(it)

                if (apAdapter == null){
                    viewModel.createView()
                }else{
                    apAdapter!!.items.add(mutableMapOf(FileName().list to it))
                    apAdapter?.notifyItemInserted(apAdapter!!.itemCount - 1)
                    // Log.d("event", viewModel.todoList.value!!.peekContent.toString())

                    // addListによってListが新たに追加されたことをNavに通知
                    navigationViewModel.setInsertFlag()
                }
                // ネットワークに接続されている場合のみ、FirebaseStoreからデータをダウンロード
                if (networkStatus == true){
                    // TodoActivityで端末がoffline状態になった時の対応
                    if (viewModel.connectingStatus() != null){
                        viewModel.upload()
                    }else{
                        networkStatus = false
                        NetWorkFailureDialog(flag = false).show(requireActivity().supportFragmentManager, "NetWorkFailureDialog")
                    }
                }
            }
        })
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        var retValue = true

        when(item.itemId){
            1 -> {
                val position = apAdapter!!.getPosition()
                Log.d("context", position.toString())

                val list = apAdapter!!.items[position][FileName().list]
                privateViewModel.deletePreference(list!!)

                apAdapter!!.items.removeAt(position)
                apAdapter?.notifyItemRemoved(position)

                // コンテキストメニューからリストが削除されたことをNavに通知
                navigationViewModel.setRemoveFlag(position)

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
                            privateViewModel.delete(position, flag = true)
                            privateViewModel.delete(position, flag = false)
                        }else{
                            networkStatus = false
                            NetWorkFailureDialog(flag = false).show(requireActivity().supportFragmentManager, "NetWorkFailureDialog")
                        }
                    }
                    // 内部ストレージからtaskファイルを完全削除する
                    privateViewModel.listDelete(position)
                }else{
                    /*
                     内部ストレージのlistから該当task名を削除
                     該当taskも削除
                     ネットワークに接続されている場合のみ、FirebaseStoreからデータを削除
                     */
                    if (networkStatus == true){
                        // TodoActivityで端末がoffline状態になった時の対応
                        if (viewModel.connectingStatus() != null){
                            privateViewModel.delete(position, flag = false)
                        }else{
                            networkStatus = false
                            NetWorkFailureDialog(flag = false).show(requireActivity().supportFragmentManager, "NetWorkFailureDialog")
                        }
                    }
                    privateViewModel.listDelete(position)
                    // ネットワークに接続されている場合のみ、FirebaseStorageのlist更新
                    if (networkStatus == true){
                        // TodoActivityで端末がoffline状態になった時の対応
                        if (viewModel.connectingStatus() != null){
                            viewModel.upload()
                        }else{
                            networkStatus = false
                            NetWorkFailureDialog(flag = false).show(requireActivity().supportFragmentManager, "NetWorkFailureDialog")
                        }
                    }
                }
            }else -> retValue = super.onContextItemSelected(item)
        }
        return retValue
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}