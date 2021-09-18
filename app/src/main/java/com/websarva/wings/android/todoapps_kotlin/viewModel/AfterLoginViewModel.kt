package com.websarva.wings.android.todoapps_kotlin.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AfterLoginViewModel: ViewModel() {
    private val _networkStatus = MutableLiveData<Boolean>().apply {
        MutableLiveData<Boolean>()
    }

    fun init(networkStatus: Boolean){
        _networkStatus.value = networkStatus
    }

    fun networkStatus(): MutableLiveData<Boolean>{
        return _networkStatus
    }
}