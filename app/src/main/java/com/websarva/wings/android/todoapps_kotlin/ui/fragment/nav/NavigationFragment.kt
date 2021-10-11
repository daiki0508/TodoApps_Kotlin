package com.websarva.wings.android.todoapps_kotlin.ui.fragment.nav

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.databinding.FragmentNavigationBinding
import com.websarva.wings.android.todoapps_kotlin.model.IntentBundle
import com.websarva.wings.android.todoapps_kotlin.model.NavNotify
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.add.AddTodoTaskFragment
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.nav.navigationDrawer.NavRecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.nav.navigationDrawer.NavTopRecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.TodoFragment
import com.websarva.wings.android.todoapps_kotlin.ui.settings.SettingsActivity
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.AfterLoginViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.nav.NavigationViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.nav.PrivateNavigationViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.todo.TodoViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class NavigationFragment: Fragment() {
    private var _binding: FragmentNavigationBinding? = null
    private val binding
    get() = _binding!!

    private val viewModelPrivate: PrivateNavigationViewModel by viewModel()
    private val viewModel: NavigationViewModel by sharedViewModel()
    private val todoViewModel: TodoViewModel by sharedViewModel()
    private val afterLoginViewModel: AfterLoginViewModel by activityViewModels()

    private var nvAdapter: NavRecyclerViewAdapter? = null
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var networkStatus: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNavigationBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        networkStatus = afterLoginViewModel.networkStatus().value

        binding.navRecyclerView.layoutManager = LinearLayoutManager(requireActivity())

        // ネットワークの接続状態によって処理を分岐
        if (networkStatus == true){
            viewModelPrivate.init(Firebase.auth)
        }

        viewModel.flag().observe(this.viewLifecycleOwner, { flag ->
            activity?.let {
                // タスク一覧
                val nvTopAdapter = NavTopRecyclerViewAdapter(type = 0, flag = flag, it)
                binding.navTopRecyclerView.adapter = nvTopAdapter
                binding.navTopRecyclerView.addItemDecoration(DividerItemDecoration(it, DividerItemDecoration.VERTICAL))
                binding.navTopRecyclerView.layoutManager = LinearLayoutManager(it)

                nvTopAdapter.setOnItemClickListener(object: OnItemClickListener {
                    override fun onItemClickListener(view: View, position: Int, list: String?) {
                        // TodoFragmentへ遷移
                        val transactionNvTop = requireActivity().supportFragmentManager.beginTransaction()
                        transactionNvTop.replace(R.id.nav_fragment, this@NavigationFragment)
                        transactionNvTop.replace(R.id.container, TodoFragment()).commit()
                    }
                })
            }
        })
        activity?.let {
            // 設定
            val nvSettingsAdapter = NavTopRecyclerViewAdapter(type = 1, flag = true, it)
            binding.navFooterRecyclerView.adapter = nvSettingsAdapter
            binding.navFooterRecyclerView.addItemDecoration(DividerItemDecoration(it, DividerItemDecoration.VERTICAL))
            binding.navFooterRecyclerView.layoutManager = LinearLayoutManager(it)

            nvSettingsAdapter.setOnItemClickListener(object: OnItemClickListener {
                override fun onItemClickListener(view: View, position: Int, list: String?) {
                    // SettingsActivityへ遷移
                    Intent(it, SettingsActivity::class.java).apply {
                        this.putExtra(IntentBundle.NetworkStatus.name, afterLoginViewModel.networkStatus().value)
                        startActivity(this)

                        // 終了処理
                        it.finish()
                    }
                }
            })
        }

        // NavigationDrawer
        viewModel.position().observe(this.viewLifecycleOwner, {
            nvAdapter = NavRecyclerViewAdapter(
                todoViewModel.todoList.value!!.peekContent,
                it,
                todoViewModel = todoViewModel,
                navViewModelPrivate = viewModelPrivate,
                requireActivity()
            )
            binding.navRecyclerView.adapter = nvAdapter
            itemTouchHelper =
                ItemTouchHelper(nvAdapter!!.getRecyclerViewSimpleCallBack(todoViewModel.apAdapter().value))
            itemTouchHelper.attachToRecyclerView(binding.navRecyclerView)

            // navigationDrawerのアイテムタップ時の処理
            nvAdapter!!.setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClickListener(view: View, position: Int, list: String?) {
                    // bundleに値をセット
                    viewModelPrivate.setBundle(list!!, position)
                }
            })
        })

        // insertFlagのobserver
        viewModel.insertFlag().observe(this.viewLifecycleOwner, {
            if (it){
                // nvAdapterを更新
                // Log.d("event2", todoViewModel.todoList.value!!.peekContent.toString())
                nvAdapter?.notifyItemInserted(nvAdapter?.itemCount!! - 1)
            }
        })

        // removeFlagのobserver
        viewModel.removeFlag().observe(this.viewLifecycleOwner, {
            if (it[NavNotify.Flag.name] as Boolean){
                // nvAdapterを更新
                nvAdapter?.notifyItemRemoved(it[NavNotify.Position.name] as Int)
            }
        })

        // changeFlagのobserver
        viewModel.changeFlag().observe(this.viewLifecycleOwner, {
            if (it[NavNotify.Flag.name] as Boolean){
                // nvAdapterを更新
                nvAdapter?.notifyItemChanged(it[NavNotify.Position.name] as Int)
            }
        })

        // bundleのobserver
        viewModelPrivate.bundle.observe(this.viewLifecycleOwner, { event ->
            val transactionAddTask = requireActivity().supportFragmentManager.beginTransaction()
            event?.contentIfNotHandled.let {
                if (it != null){
                    AddTodoTaskFragment().apply {
                        this.arguments = it

                        // AddTodoTaskFragmentに遷移
                        transactionAddTask.replace(R.id.nav_fragment, this@NavigationFragment)
                        transactionAddTask.replace(R.id.container, this).commit()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}