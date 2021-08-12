package com.websarva.wings.android.todoapps_kotlin.ui.todo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityTodoBinding

class TodoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTodoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTodoBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }
    }
}