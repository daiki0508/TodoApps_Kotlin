package com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.nav

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.websarva.wings.android.todoapps_kotlin.model.NavNotify

class NavigationViewModel: ViewModel() {
    private val _flag = MutableLiveData<Boolean>()
    private val _position = MutableLiveData<Int>()
    private val _insertFlag = MutableLiveData<Boolean>()
    private val _removeFlag = MutableLiveData<MutableMap<String, Any>>()
    private val _changeFlag = MutableLiveData<MutableMap<String, Any>>()

    fun setFlag(flag: Boolean){
        _flag.value = flag
    }
    fun setPosition(position: Int){
        _position.value = position
    }
    fun setInsertFlag(){
        _insertFlag.value = true
    }
    fun setRemoveFlag(position: Int){
        _removeFlag.value = mutableMapOf(NavNotify.Flag.name to true, NavNotify.Position.name to position)
    }
    fun setChangeFlag(position: Int){
        _removeFlag.value = mutableMapOf(NavNotify.Flag.name to true, NavNotify.Position.name to position)
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
    fun removeFlag(): MutableLiveData<MutableMap<String, Any>>{
        return _removeFlag
    }
    fun changeFlag(): MutableLiveData<MutableMap<String, Any>>{
        return _removeFlag
    }

    init {
        _insertFlag.value = false
        _removeFlag.value = mutableMapOf(NavNotify.Flag.name to false, NavNotify.Position.name to 0)
        _changeFlag.value = mutableMapOf(NavNotify.Flag.name to false, NavNotify.Position.name to 0)
    }
}