package com.websarva.wings.android.todoapps_kotlin.ui.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivitySettingsBinding
import com.websarva.wings.android.todoapps_kotlin.ui.add.AddTodoTaskActivity
import com.websarva.wings.android.todoapps_kotlin.ui.todo.TodoActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private lateinit var list: String
    private var position: Int? = null
    private var flag: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

        // falseがAddTodoTaskActivity
        flag = intent.getBooleanExtra("flag", true)
        if (!flag!!){
            list = intent.getStringExtra("list")!!
            position = intent.getIntExtra("position", 0)
        }
    }

    override fun onBackPressed() {
        // trueがTodoActivity
        if (flag!!){
            Intent(this, TodoActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }else{
            Intent(this, AddTodoTaskActivity::class.java).apply {
                this.putExtra("list", list)
                this.putExtra("position", position)
                startActivity(this)
                finish()
            }
        }
    }
}