package com.websarva.wings.android.todoapps_kotlin.ui.fragment.nav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.websarva.wings.android.todoapps_kotlin.databinding.FragmentNavigationBinding
import com.websarva.wings.android.todoapps_kotlin.ui.OnItemClickListener
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.nav.navigationDrawer.NavRecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.nav.navigationDrawer.NavTopRecyclerViewAdapter
import com.websarva.wings.android.todoapps_kotlin.viewModel.NavigationViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.TodoViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class NavigationFragment: Fragment() {
    private var _binding: FragmentNavigationBinding? = null
    private val binding
    get() = _binding!!

    private val viewModel: NavigationViewModel by viewModel()
    private val todoViewModel: TodoViewModel by sharedViewModel()

    private var nvAdapter: NavRecyclerViewAdapter? = null
    private lateinit var itemTouchHelper: ItemTouchHelper

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

        binding.navRecyclerView.layoutManager = LinearLayoutManager(requireActivity())

        activity?.let {
            // タスク一覧
            val nvTopAdapter = NavTopRecyclerViewAdapter(type = 0, flag = true, it)
            binding.navTopRecyclerView.adapter = nvTopAdapter
            binding.navTopRecyclerView.addItemDecoration(DividerItemDecoration(it, DividerItemDecoration.VERTICAL))
            binding.navTopRecyclerView.layoutManager = LinearLayoutManager(it)

            // 設定
            val nvSettingsAdapter = NavTopRecyclerViewAdapter(type = 1, flag = true, it)
            binding.navFooterRecyclerView.adapter = nvSettingsAdapter
            binding.navFooterRecyclerView.addItemDecoration(DividerItemDecoration(it, DividerItemDecoration.VERTICAL))
            binding.navFooterRecyclerView.layoutManager = LinearLayoutManager(it)

            nvSettingsAdapter.setOnItemClickListener(object: OnItemClickListener{
                override fun onItemClickListener(view: View, position: Int, list: String?) {
                    /*Intent(this@TodoFragment, SettingsActivity::class.java).apply {
                        this.putExtra("flag", true)
                        this.putExtra("network", networkStatus)
                        startActivity(this)
                        finish()
                    }*/
                }
            })
        }

        // NavigationDrawer
        todoViewModel.todoList().observe(this.viewLifecycleOwner, {
            nvAdapter = NavRecyclerViewAdapter(it, -1, todoViewModel = todoViewModel, navViewModel = viewModel, addTodoTaskViewModel = null, requireActivity())
            binding.navRecyclerView.adapter = nvAdapter
            itemTouchHelper = ItemTouchHelper(nvAdapter!!.getRecyclerViewSimpleCallBack(todoViewModel.apAdapter().value))
            itemTouchHelper.attachToRecyclerView(binding.navRecyclerView)

            nvAdapter!!.setOnItemClickListener(object: OnItemClickListener {
                override fun onItemClickListener(view: View, position: Int, list: String?) {
                    //addTodoIntent(list!!, position)
                }
            })
        })
    }
}