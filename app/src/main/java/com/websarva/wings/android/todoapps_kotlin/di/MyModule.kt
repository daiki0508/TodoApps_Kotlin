package com.websarva.wings.android.todoapps_kotlin.di

import com.websarva.wings.android.todoapps_kotlin.viewModel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class MyModule {
    val module: Module = module {
        viewModel { MainViewModel() }
    }
}