package com.websarva.wings.android.todoapps_kotlin.di

import android.app.Application
import com.websarva.wings.android.todoapps_kotlin.di.MyModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(listOf(MyModule().module, MyModule().repository))
        }
    }
}