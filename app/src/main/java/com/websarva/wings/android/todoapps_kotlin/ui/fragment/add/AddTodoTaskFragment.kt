package com.websarva.wings.android.todoapps_kotlin.ui.fragment.add

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.BuildConfig
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.databinding.FragmentAddTodoListBinding
import com.websarva.wings.android.todoapps_kotlin.model.DialogBundle
import com.websarva.wings.android.todoapps_kotlin.model.DownloadStatus
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.model.IntentBundle
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.OnPreferenceListener
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.add.recyclerView.*
import com.websarva.wings.android.todoapps_kotlin.ui.NetWorkFailureDialog
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.TodoFragment
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.add.AddTodoTaskViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.AfterLoginViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.nav.NavigationViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.todo.TodoViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class AddTodoTaskFragment : Fragment(){
    private var _binding: FragmentAddTodoListBinding? = null
    private val binding
    get() = _binding!!

    private val viewModel: AddTodoTaskViewModel by viewModel()
    private val afterLoginViewModel by activityViewModels<AfterLoginViewModel>()
    private val todoViewModel: TodoViewModel by sharedViewModel()
    private val navigationViewModel: NavigationViewModel by sharedViewModel()

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var task: String
    private var position = 0
    private var networkStatus: Boolean? = null
    private var apAdapter: RecyclerViewAdapter? = null
    private var acAdapter: ChildRecyclerViewAdapter? = null
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AddListDialog????????????????????????
        setFragmentResultListener(DialogBundle.Result.name){ _, data ->
            viewModel.setData(data)
        }

        // navigation?????????
        navigationViewModel.setFlag(flag = false)

        // completeFlag???????????????
        todoViewModel.setCompleteFlag(mutableMapOf(
            DownloadStatus().list to null,
            DownloadStatus().iv_aes_list to null,
            DownloadStatus().salt_list to null,
            DownloadStatus().task to null,
            DownloadStatus().iv_aes_task to null,
            DownloadStatus().salt_task to null
        ))

        requireActivity().onBackPressedDispatcher.addCallback(this){
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.nav_up_pop_enter_anim, R.anim.nav_up_pop_exit_anim)
            transaction.replace(R.id.container, TodoFragment()).commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTodoListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!BuildConfig.DEBUG){
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }

        // OptionsMenu????????????
        setHasOptionsMenu(true)

        task = arguments?.getString(FileName().list)!!
        position = arguments?.getInt(IntentBundle.Position.name, 0)!!
        networkStatus = afterLoginViewModel.networkStatus().value

        // navigation?????????
        navigationViewModel.setPosition(position)

        // ??????????????????????????????????????????????????????????????????????????????
        if (networkStatus == true){
            auth = Firebase.auth
            storage = FirebaseStorage.getInstance()

            viewModel.setInit(list = task, auth, storage, networkStatus!!)
        }else{
            viewModel.setInit(list = task, auth = null, storage = null, networkStatus!!)
        }

        viewModel.showBalloonFlag(this)
        with(viewModel){
            contentBalloon2().observe(this@AddTodoTaskFragment.viewLifecycleOwner, {
                // balloon????????????????????????
                fabBalloon().value!!.relayShowAlignBottom(contentBalloon0().value!!, binding.tvNoContent)
                    .relayShowAlignBottom(contentBalloon1().value!!, binding.tvNoContent)
                    .relayShowAlignBottom(it, binding.tvNoContent)

                // balloon?????????
                fabBalloon().value!!.showAlignTop(binding.fab)

                it.setOnBalloonDismissListener {
                    // ???????????????????????????
                    save()
                    // balloonComplete?????????
                    setBalloonComplete()
                }
            })

            // balloonComplete???observer
            balloonComplete().observe(this@AddTodoTaskFragment.viewLifecycleOwner, { flag ->
                if (flag){
                    if (File(requireActivity().filesDir, "task/$task/${FileName().task}").length() != 0L){
                        viewModel.createView()
                    }
                }
            })
        }

        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
        binding.unCompleteCount.visibility = View.GONE

        viewModel.todoTask().observe(this.viewLifecycleOwner, {
            if (it.isNotEmpty()){
                if (apAdapter == null){
                    // recyclerview???deleteTask???????????????????????????????????????????????????NoContents?????????????????????
                    binding.tvNoContent.visibility = View.GONE
                    binding.recyclerview.visibility = View.VISIBLE
                    binding.unCompleteCount.visibility = View.VISIBLE

                    acAdapter = ChildRecyclerViewAdapter(it, viewModel)
                    itemTouchHelper = ItemTouchHelper(acAdapter!!.getRecyclerViewSimpleCallBack())

                    apAdapter = RecyclerViewAdapter(itemTouchHelper, task, this, viewModel, acAdapter)
                    binding.recyclerview.adapter = apAdapter

                    apAdapter?.setOnItemClickListener(object: OnItemClickListener {
                        override fun onItemClickListener(view: View, position: Int, list: String?) {
                            // list?????????
                            viewModel.setPosition(this@AddTodoTaskFragment.position)
                            AddListDialog(flag = false, type = 1, position = position).show(requireActivity().supportFragmentManager, "UpdateListDialog")
                        }
                    })

                    acAdapter?.setPreferenceListener(object: OnPreferenceListener {
                        override fun onPreferenceWriteListener(position: Int, keyName: String, checkFlag: Boolean) {
                            // checkBox?????????????????????UI?????????
                            viewModel.writePreference(keyName, checkFlag)
                            acAdapter?.notifyItemChanged(position)

                            // nvAdapter?.notifyItemChanged(position)
                            navigationViewModel.setChangeFlag(position)
                        }

                        override fun onPreferenceReadListener(keyName: String): Boolean {
                            // ??????????????????????????????textview???NavigationDrawer?????????
                            binding.unCompleteCount.text = getString(R.string.unCompleteTaskCount, viewModel.countUnCompleteTask(acAdapter!!.items))
                            // nvAdapter?.notifyItemChanged(this@AddTodoTaskFragment.position)
                            navigationViewModel.setChangeFlag(this@AddTodoTaskFragment.position)

                            return viewModel.readPreference(keyName)
                        }
                    })
                }
            }
        })

        viewModel.data().observe(this.viewLifecycleOwner, {
            if (it.size() != 0){
                // bundle?????????????????????
                val list = it.getString(DialogBundle.List.name)!!
                val type = it.getInt(DialogBundle.Type.name)
                val flag = it.getBoolean(DialogBundle.Flag.name)
                val position = it.getInt(DialogBundle.Position.name)

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
                // true???????????????false????????????
                if (flag){
                    when {
                        // task?????????
                        type == 1 -> {
                            viewModel.writePreference(keyName = acAdapter!!.items[position][FileName().task]!!, checkFlag = false)
                            acAdapter!!.items[position][FileName().task] = list
                            acAdapter!!.notifyItemChanged(position)
                        }
                        acAdapter == null -> {
                            // ?????????task?????????
                            viewModel.createView()
                        }
                        else -> {
                            // task?????????
                            acAdapter!!.items.add(mutableMapOf(FileName().task to list))
                            acAdapter!!.notifyItemInserted(acAdapter!!.itemCount - 1)
                        }
                    }
                    // ???????????????????????????????????????????????????????????????????????????
                    if (networkStatus == true){
                        if (viewModel.connectingStatus() != null){
                            viewModel.upload(flag)
                        }else{
                            networkStatus = false
                            NetWorkFailureDialog(flag = false).show(requireActivity().supportFragmentManager, "NetWorkFailureDialog")
                        }
                    }
                }else{
                    // ?????????????????????????????????????????????????????????????????????????????????????????????
                    if (networkStatus == true){
                        if (viewModel.connectingStatus() != null){
                            viewModel.upload(flag)
                            viewModel.upload(flag = true)
                            viewModel.delete()
                        }else{
                            networkStatus = false
                            NetWorkFailureDialog(flag = false).show(requireActivity().supportFragmentManager, "NetWorkFailureDialog")
                        }
                    }

                    // preference????????????move
                    for (i in 0 until acAdapter!!.items.size){
                        viewModel.writePreference(
                            keyName = acAdapter!!.items[i][FileName().task]!!,
                            checkFlag = viewModel.readPreference(acAdapter!!.items[i][FileName().task]!!))
                    }
                    // ???preference?????????
                    viewModel.deletePreference(task)

                    apAdapter!!.task = list
                    apAdapter!!.notifyItemChanged(position)
                    task = list
                }
            }
        })

        binding.fab.setOnClickListener {
            AddListDialog(flag = true, type = 0, position = null).show(requireActivity().supportFragmentManager, "AddTaskDialog")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.toolbar_options_menu, menu)
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
            // task?????????
            1 -> {
                val position = acAdapter!!.getPosition()
                Log.d("context", position.toString())
                acAdapter!!.items.removeAt(position)
                acAdapter!!.notifyItemRemoved(position)

                // Nav??????????????????
                navigationViewModel.setChangeFlag(this.position)

                /*
                 task????????????0???????????????recyclerview?????????????????????NoContent?????????
                 acAdapter???apAdapter????????????
                 */
                if (acAdapter!!.itemCount == 0){
                    binding.recyclerview.visibility = View.GONE
                    binding.tvNoContent.visibility = View.VISIBLE
                    binding.unCompleteCount.visibility = View.GONE

                    acAdapter = null
                    apAdapter = null
                    /*
                     FirebaseStorage??????task???????????????
                     ???????????????????????????????????????????????????????????????
                     */
                    if (networkStatus == true){
                        if (viewModel.connectingStatus() != null){
                            viewModel.delete()
                        }else{
                            networkStatus = false
                            NetWorkFailureDialog(flag = false).show(requireActivity().supportFragmentManager, "NetWorkFailureDialog")
                        }
                    }
                    // ???????????????????????????task?????????????????????????????????
                    viewModel.taskDelete(position)
                }else{
                    binding.unCompleteCount.text = getString(R.string.unCompleteTaskCount, viewModel.countUnCompleteTask(acAdapter!!.items))

                    // ?????????????????????????????????task?????????
                    viewModel.taskDelete(position)
                    /*
                     FirebaseStorage?????????
                     ???????????????????????????????????????????????????????????????????????????
                     */
                    if (networkStatus == true){
                        if (viewModel.connectingStatus() != null){
                            viewModel.upload(flag = true)
                        }else{
                            networkStatus = false
                            NetWorkFailureDialog(flag = false).show(requireActivity().supportFragmentManager, "NetWorkFailureDialog")
                        }
                    }
                }
            }
            else -> retValue = super.onContextItemSelected(item)
        }
        return retValue
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}