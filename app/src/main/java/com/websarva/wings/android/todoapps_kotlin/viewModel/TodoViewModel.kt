package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.model.DownloadStatus
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.OffLineRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.PreferenceRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.TodoListEvent
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.recyclerView.RecyclerViewAdapter
import kotlinx.coroutines.*
import java.io.File

class TodoViewModel(
    application: Application
): AndroidViewModel(application) {
    private val _context = MutableLiveData<Context>()
    private val _todoList = MutableLiveData<TodoListEvent<MutableList<MutableMap<String, String>>>>()
    val todoList: LiveData<TodoListEvent<MutableList<MutableMap<String, String>>>> = _todoList
    private val _storage = MutableLiveData<FirebaseStorage>().apply {
        MutableLiveData<FirebaseStorage>()
    }
    private val _auth = MutableLiveData<FirebaseAuth>().apply {
        MutableLiveData<FirebaseAuth>()
    }
    private val _networkStatus = MutableLiveData<Boolean>()
    private val _completeFlag = MutableLiveData<MutableMap<String, Boolean?>>()
    private val _apAdapter = MutableLiveData<RecyclerViewAdapter>()

    fun connectingStatus(): NetworkCapabilities? {
        val connectivityManager =
            getApplication<Application>().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 戻り値がnullでなければ、ネットワークに接続されている
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    }
    fun createView(){
        // ネットワークの接続状況によって処理を分割
        val lists: String? = if (_networkStatus.value == true){
            OffLineRepositoryClient().online(_context.value!!, _auth.value!!)
            CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(_context.value!!, OffLineRepositoryClient().read(_context.value!!)!!.toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        }

        //Log.d("lists", lists!!)
        _todoList.value = TodoListEvent(createTodoContents(lists, FileName().list))
    }
    fun createTodoContents(contents: String?, keyName: String): MutableList<MutableMap<String, String>>{
        val todoContents: MutableList<MutableMap<String, String>> = mutableListOf()
        var todo: MutableMap<String, String>

        if (contents!!.isNotBlank()){
            for (content in contents.split(" ")){
                todo = mutableMapOf(keyName to content)
                todoContents.add(todo)
            }
        }else{
            todo = mutableMapOf(keyName to contents)
            todoContents.add(todo)
        }
        return todoContents
    }
    fun upload(){
        FirebaseStorageRepositoryClient().upload(_context.value!!, _storage.value!!, _auth.value!!, task = null, flag = false)
    }
    fun readPreference(list: String, keyName: String): Boolean{
        return PreferenceRepositoryClient().read(_context.value!!, list, keyName)
    }
    fun apAdapter(): MutableLiveData<RecyclerViewAdapter>{
        return _apAdapter
    }
    fun completeFlag(): MutableLiveData<MutableMap<String, Boolean?>>{
        return _completeFlag
    }

    fun setInit(auth: FirebaseAuth?, storage: FirebaseStorage?, networkStatus: Boolean){
        _auth.value = auth
        _storage.value = storage
        _networkStatus.value = networkStatus
    }
    fun setAdapter(adapter: RecyclerViewAdapter){
        _apAdapter.value = adapter
    }
    fun setCompleteFlag(taskMap: MutableMap<String, Boolean?>){
        _completeFlag.value = taskMap
    }

    init {
        _context.value = getApplication<Application>().applicationContext

        _completeFlag.value = mutableMapOf(
            DownloadStatus().list to null,
            DownloadStatus().iv_aes_list to null,
            DownloadStatus().salt_list to null,
            DownloadStatus().task to null,
            DownloadStatus().iv_aes_task to null,
            DownloadStatus().salt_task to null
        )
    }
}