package com.websarva.wings.android.todoapps_kotlin.di

import com.websarva.wings.android.todoapps_kotlin.repository.*
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.AfterLoginViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.add.AddTodoTaskViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.nav.NavigationViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.nav.PrivateNavigationViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.todo.PrivateTodoViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.todo.TodoViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.main.MainViewModel
import com.websarva.wings.android.todoapps_kotlin.viewModel.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class MyModule {
    val module: Module = module {
        viewModel { MainViewModel(get(), get()) }
        viewModel { AfterLoginViewModel() }
        viewModel { TodoViewModel(get()) }
        viewModel { PrivateTodoViewModel(get(), get(), get(), get(), get()) }
        viewModel { PrivateNavigationViewModel(get(), get()) }
        viewModel { NavigationViewModel() }
        viewModel { AddTodoTaskViewModel(get(), get(), get(), get(), get()) }
        viewModel { SettingsViewModel(get(), get(), get(), get()) }
    }
    val repository: Module = module {
        factory { AppUpdateRepositoryClient() }
        factory { PreferenceBalloonRepositoryClient() }
        factory { FirebaseTopRepositoryClient() }
        factory { FirebaseStorageRepositoryClient() }
        factory { PreferenceRepositoryClient() }
        factory { OffLineRepositoryClient() }
    }
}