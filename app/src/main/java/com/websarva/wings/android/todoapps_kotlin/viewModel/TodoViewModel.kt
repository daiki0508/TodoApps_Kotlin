package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.content.Context
import android.util.Log
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageDownloadRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageUploadRepositoryClient
import kotlinx.coroutines.*

class TodoViewModel(
    private val firebaseStorageUploadRepository: FirebaseStorageUploadRepositoryClient,
    private val firebaseStorageDownloadRepository: FirebaseStorageDownloadRepositoryClient
): ViewModel() {
    private val _todoList = MutableLiveData<MutableList<MutableMap<String, String>>>().apply {
        MutableLiveData<MutableList<MutableMap<String, String>>>()
    }

    fun upload(context: Context, storage: FirebaseStorage, auth: FirebaseAuth){
        firebaseStorageUploadRepository.upload(context, storage, auth)
    }

    fun download(context: Context, storage: FirebaseStorage, auth: FirebaseAuth){
        firebaseStorageDownloadRepository.download(context, storage, auth)
    }

    fun createView(context: Context, auth: FirebaseAuth){
        val lists = CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "", false)

        val todoList: MutableList<MutableMap<String, String>> = mutableListOf()
        var todo: MutableMap<String, String>
        for (list in lists?.split(" ")!!){
            Log.d("test", list)
            todo = mutableMapOf("list" to list, "content" to "")
            todoList.add(todo)
        }
        _todoList.value = todoList
    }

    fun todoList(): MutableLiveData<MutableList<MutableMap<String, String>>>{
        return _todoList
    }

    init {
        _todoList.value = mutableListOf()
    }
}