package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.app.Activity
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
import com.websarva.wings.android.todoapps_kotlin.repository.PreferenceRepositoryClient
import kotlinx.coroutines.*
import java.io.File

class TodoViewModel(
    private val firebaseStorageRepository: FirebaseStorageRepositoryClient,
    private val preferenceRepository: PreferenceRepositoryClient
): ViewModel() {
    private val _todoList = MutableLiveData<MutableList<MutableMap<String, String>>>().apply {
        MutableLiveData<MutableList<MutableMap<String, String>>>()
    }
    private val _completeFlag = MutableLiveData<MutableMap<String, Boolean>>().apply {
        MutableLiveData<MutableMap<String, Boolean>>()
    }

    fun upload(context: Context, storage: FirebaseStorage, auth: FirebaseAuth){
        firebaseStorageRepository.upload(context, storage, auth, task = null, flag = false)
    }

    fun download(context: Context, storage: FirebaseStorage, auth: FirebaseAuth, flag: Boolean){
        if (flag){
            firebaseStorageRepository.download(context, addViewModel = null, this, storage, auth, tasks = null, flag)
        }else{
            if (File(context.filesDir, "list").exists()){
                val lists = CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag)
                firebaseStorageRepository.download(context, addViewModel = null, this, storage, auth, lists, flag = flag)
            }
        }
    }

    fun delete(storage: FirebaseStorage, auth: FirebaseAuth){
        firebaseStorageRepository.delete(storage, auth, task = null, flag = true)
    }

    fun readPreference(activity: Activity, task: String, keyName: String): Boolean{
        return preferenceRepository.read(activity, task, keyName)
    }

    fun createView(context: Context, auth: FirebaseAuth){
        val lists = CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)

        _todoList.value = createTodoContents(lists, "list")
    }

    fun getTask(context: Context, auth: FirebaseAuth, task: String): MutableList<MutableMap<String, String>>{
        val taskFile = File("${context.filesDir}/task/$task/task")
        val tasks: String? = if (taskFile.exists() || taskFile.length() != 0L){
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

    fun listDelete(context: Context, auth: FirebaseAuth, position: Int){
        val tasksBefore = CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        Log.d("update_b", tasksBefore!!)
        val tasksAfter = tasksBefore!!.replace("${tasksBefore.split(" ")[position]} ", "")
        if (tasksBefore == tasksAfter){
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "", type = 6, task = null, aStr = null, flag = true)
        }else{
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 4, task = null, aStr = null, flag = true)
        }
        Log.d("update_a", tasksAfter)
    }

    fun completeFlag(): MutableLiveData<MutableMap<String, Boolean>>{
        return _completeFlag
    }

    fun todoList(): MutableLiveData<MutableList<MutableMap<String, String>>>{
        return _todoList
    }

    fun setCompleteFlag(taskMap: MutableMap<String, Boolean>){
        _completeFlag.value = taskMap
    }

    init {
        _completeFlag.value = mutableMapOf(
            "list_list" to false,
            "iv_aes_list" to false,
            "salt_list" to false,
            "task_task" to false,
            "iv_aes_task" to false,
            "salt_task" to false
        )
        _todoList.value = mutableListOf()
    }
}