package com.websarva.wings.android.todoapps_kotlin.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NavigationViewModel: ViewModel() {
    private val _flag = MutableLiveData<Boolean>()
    private val _position = MutableLiveData<Int>()
    private val _insertFlag = MutableLiveData<Boolean>()

    fun setFlag(flag: Boolean){
        _flag.value = flag
    }
    fun setPosition(position: Int){
        _position.value = position
    }
    fun setInsertFlag(){
        _insertFlag.value = true
    }

    fun flag(): MutableLiveData<Boolean> {
        return _flag
    }
    fun position(): MutableLiveData<Int>{
        return _position
    }
    fun insertFlag(): MutableLiveData<Boolean>{
        return _insertFlag
    }

    init {
        _insertFlag.value = false
    }
}