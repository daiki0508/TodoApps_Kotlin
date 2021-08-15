package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageUploadRepositoryClient

class AddTodoTaskViewModel(
    private val firebaseStorageUploadRepository: FirebaseStorageUploadRepositoryClient
): ViewModel() {
    private val _todoTask = MutableLiveData<MutableList<MutableMap<String, String>>>().apply {
        MutableLiveData<MutableList<MutableMap<String, String>>>()
    }

    fun upload(context: Context, storage: FirebaseStorage, auth: FirebaseAuth, task: String){
        //TODO("未実装")
        firebaseStorageUploadRepository.upload(context, storage, auth, task, flag = false)
    }

    fun createView(context: Context, auth: FirebaseAuth, task: String){
        val tasks = CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 1, task, flag = false)

        val todoTask: MutableList<MutableMap<String, String>> = mutableListOf()
        var todo: MutableMap<String, String>
        for (list in tasks?.split(" ")!!){
            Log.d("test", list)
            todo = mutableMapOf("task" to list)
            todoTask.add(todo)
        }
        _todoTask.value = todoTask
    }

    fun todoTask(): MutableLiveData<MutableList<MutableMap<String, String>>>{
        return _todoTask
    }

    init {
        _todoTask.value = mutableListOf()
    }
}