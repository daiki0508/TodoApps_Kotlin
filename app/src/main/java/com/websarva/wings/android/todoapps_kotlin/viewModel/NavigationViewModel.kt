package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.OffLineRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.PreferenceRepositoryClient
import java.io.File

class NavigationViewModel(
    private val offLineRepository: OffLineRepositoryClient,
    private val preferenceRepository: PreferenceRepositoryClient,
    application: Application
) : AndroidViewModel(application) {
    private val _context = MutableLiveData<Context>().apply {
        MutableLiveData<Context>()
    }
    private val _auth = MutableLiveData<FirebaseAuth>().apply {
        MutableLiveData<FirebaseAuth>()
    }
    private val _networkStatus = MutableLiveData<Boolean>().apply {
        MutableLiveData<Boolean>()
    }
    private val _todoList = MutableLiveData<MutableList<MutableMap<String, String>>>().apply {
        MutableLiveData<MutableList<MutableMap<String, String>>>()
    }

    fun countUnCompleteTask(list: String, todoViewModel: TodoViewModel): Int{
        var cnt = 0

        if (File("${_context.value?.filesDir}/task/$list/${FileName().task}").length() != 0L){
            // ネットワーク接続状態によって処理を分岐
            val tasks: String? = if (todoViewModel.connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, null, flag = false)
            }else{
                CryptClass().decrypt(_context.value!!, OffLineRepositoryClient().read(_context.value!!)!!.toCharArray(), "",type = 1, list, null, flag = false)
            }

            val todoTask: MutableList<MutableMap<String, String>> = mutableListOf()
            var todo: MutableMap<String, String>
            for (task in tasks?.split(" ")!!){
                //Log.d("test", task)
                todo = mutableMapOf(FileName().task to task)
                todoTask.add(todo)
            }

            for (item in todoTask){
                if (!todoViewModel.readPreference(list, item[FileName().task]!!)){
                    cnt++
                }
            }
        }
        return cnt
    }
    fun move(items: MutableList<MutableMap<String, String>>, fromPosition: Int, toPosition: Int, todoViewModel: TodoViewModel){
        val toPositionItem = items[toPosition][FileName().list]
        val fromPositionItem = items[fromPosition][FileName().list]
        // ネットワーク接続状態によって処理を分岐
        val lists: String? = if (todoViewModel.connectingStatus() != null){
            CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        }

        //Log.d("remove_b", lists!!)
        var newLists = ""
        for ((i, value) in lists!!.split(" ").withIndex()){
            when (i) {
                toPosition -> {
                    newLists += if (i.plus(1) == items.size){
                        fromPositionItem
                    }else{
                        "$fromPositionItem "
                    }
                }
                fromPosition -> {
                    newLists += if (i.plus(1) == items.size){
                        toPositionItem
                    }else{
                        "$toPositionItem "
                    }
                }
                else -> {
                    newLists += if (i.plus(1) == items.size){
                        value
                    }else{
                        "$value "
                    }
                }
            }
        }
        //Log.d("remove_a", newLists)
        // ネットワーク接続状態によって処理を分岐
        if (todoViewModel.connectingStatus() != null){
            CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), newLists, type = 7, task = null, aStr = null, flag = true)
            todoViewModel.upload()
        }else{
            CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), newLists, type = 7, task = null, aStr = null, flag = true)
        }
    }
}