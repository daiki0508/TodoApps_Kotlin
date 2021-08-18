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
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageRepositoryClient
import kotlinx.coroutines.*
import java.io.File

class TodoViewModel(
    private val firebaseStorageRepository: FirebaseStorageRepositoryClient,
): ViewModel() {
    private val _todoList = MutableLiveData<MutableList<MutableMap<String, String>>>().apply {
        MutableLiveData<MutableList<MutableMap<String, String>>>()
    }

    fun upload(context: Context, storage: FirebaseStorage, auth: FirebaseAuth){
        firebaseStorageRepository.upload(context, storage, auth, task = null, flag = false)
    }

    fun download(context: Context, storage: FirebaseStorage, auth: FirebaseAuth){
        TODO("repositoryの引数が変更されたため")
        //firebaseStorageRepository.download(context, storage, auth, task = null, flag = true)
    }

    fun createView(context: Context, auth: FirebaseAuth){
        val lists = CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)

        _todoList.value = createTodoContents(lists, "list")
    }

    fun getTask(context: Context, auth: FirebaseAuth, task: String): MutableList<MutableMap<String, String>>{
        val tasks: String? = if (File("${context.filesDir}/task/$task").exists()){
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 1,task, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 2,task, aStr = null, flag = false)
        }

        return createTodoContents(tasks, keyName = "task")
    }

    private fun createTodoContents(contents: String?, keyName: String): MutableList<MutableMap<String, String>>{
        val todoContents: MutableList<MutableMap<String, String>> = mutableListOf()
        var todo: MutableMap<String, String>

        if (contents!!.isNotBlank()){
            for (content in contents.split(" ")){
                Log.d("test", content)
                todo = mutableMapOf(keyName to content)
                todoContents.add(todo)
            }
        }else{
            todo = mutableMapOf(keyName to contents)
            todoContents.add(todo)
        }
        return todoContents
    }

    fun todoList(): MutableLiveData<MutableList<MutableMap<String, String>>>{
        return _todoList
    }

    init {
        _todoList.value = mutableListOf()
    }
}