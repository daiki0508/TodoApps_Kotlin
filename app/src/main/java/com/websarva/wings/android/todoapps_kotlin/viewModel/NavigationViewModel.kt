package com.websarva.wings.android.todoapps_kotlin.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NavigationViewModel: ViewModel() {
    private val _flag = MutableLiveData<Boolean>().apply {
        MutableLiveData<Int>()
    }
    private val _position = MutableLiveData<Int>().apply {
        MutableLiveData<Int>()
    }

    fun setFlag(flag: Boolean){
        _flag.value = flag
    }
    fun setPosition(position: Int){
        _position.value = position
    }

    fun flag(): MutableLiveData<Boolean> {
        return _flag
    }
    fun position(): MutableLiveData<Int>{
        return _position
    }
}