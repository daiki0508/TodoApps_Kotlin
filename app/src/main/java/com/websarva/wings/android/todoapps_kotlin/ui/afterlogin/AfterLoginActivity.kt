package com.websarva.wings.android.todoapps_kotlin.ui.afterlogin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityAfterLoginBinding
import com.websarva.wings.android.todoapps_kotlin.model.IntentBundle
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.TodoFragment

class AfterLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAfterLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAfterLoginBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

        // fragmentの起動
        val transaction = supportFragmentManager.beginTransaction()
        TodoFragment().apply {
            val bundle = Bundle()
            // trueがNetWork接続状態
            bundle.putBoolean(IntentBundle.NetworkStatus.name, intent.getBooleanExtra(IntentBundle.NetworkStatus.name, false))
            this.arguments = bundle
            transaction.replace(R.id.container, TodoFragment()).commit()
        }
    }
}