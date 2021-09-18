package com.websarva.wings.android.todoapps_kotlin.ui.afterlogin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityAfterLoginBinding
import com.websarva.wings.android.todoapps_kotlin.model.IntentBundle
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.nav.NavigationFragment
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.TodoFragment
import com.websarva.wings.android.todoapps_kotlin.viewModel.AfterLoginViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AfterLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAfterLoginBinding

    private val viewModel: AfterLoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAfterLoginBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout,binding.toolbar, R.string.drawer_open, R.string.drawer_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 初期設定
        viewModel.init(intent.getBooleanExtra(IntentBundle.NetworkStatus.name, false))

        // fragmentの起動
        supportFragmentManager.beginTransaction().let {
            it.replace(R.id.container, TodoFragment())
            it.replace(R.id.nav_fragment, NavigationFragment()).commit()
        }
    }
}