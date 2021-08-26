package com.websarva.wings.android.todoapps_kotlin.di

import com.websarva.wings.android.todoapps_kotlin.repository.*
import com.websarva.wings.android.todoapps_kotlin.viewModel.AddTodoTaskViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.MainViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.TodoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class MyModule {
    val module: Module = module {
        viewModel { MainViewModel(get()) }
        viewModel { TodoViewModel(get(), get(), get()) }
        viewModel { AddTodoTaskViewModel(get(), get()) }
    }
    val repository: Module = module {
        factory { FirebaseTopRepositoryClient() }
        factory { FirebaseStorageRepositoryClient() }
        factory { PreferenceRepositoryClient() }
        factory { OffLineRepositoryClient() }
    }
}