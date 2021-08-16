package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageDeleteRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageDownloadRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageUploadRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.ui.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.add.AddTodoTaskActivity
import java.io.File

class AddTodoTaskViewModel(
    private val firebaseStorageUploadRepository: FirebaseStorageUploadRepositoryClient,
    private val firebaseStorageDownloadRepository: FirebaseStorageDownloadRepositoryClient,
    private val firebaseStorageDeleteRepository: FirebaseStorageDeleteRepositoryClient
): ViewModel() {
    private val _todoList = MutableLiveData<String>().apply {
        MutableLiveData<String>()
    }
    private val _todoTask = MutableLiveData<MutableList<MutableMap<String, String>>>().apply {
        MutableLiveData<MutableList<MutableMap<String, String>>>()
    }

    private var position: Int
    private var lastPosition: Int

    fun upload(context: Context, storage: FirebaseStorage, auth: FirebaseAuth, task: String, flag: Boolean){
        if (flag){
            firebaseStorageUploadRepository.upload(context, storage, auth, task, flag = false)
        }else{
            firebaseStorageUploadRepository.upload(context, storage, auth, task, flag = true)
        }
    }

    fun download(context: Context, storage: FirebaseStorage, auth: FirebaseAuth, task: String, flag: Boolean){
        if (flag){
            firebaseStorageDownloadRepository.download(context, storage, auth, task, flag = false)
        }else{
            firebaseStorageDownloadRepository.download(context, storage, auth, task, flag = true)
        }
    }

    fun delete(storage: FirebaseStorage, auth: FirebaseAuth, task: String){
        firebaseStorageDeleteRepository.delete(storage, auth, task)
    }

    fun createView(context: Context, auth: FirebaseAuth, task: String){
        val tasks = CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 1, task, null, flag = false)

        val todoTask: MutableList<MutableMap<String, String>> = mutableListOf()
        var todo: MutableMap<String, String>
        for (list in tasks?.split(" ")!!){
            Log.d("test", list)
            todo = mutableMapOf("task" to list)
            todoTask.add(todo)
        }
        _todoTask.value = todoTask
    }

    fun setListName(name: String){
        _todoList.value = name
    }

    fun update(
        context: Context,
        auth: FirebaseAuth,
        task: String,
        aStr: String,
        flag: Boolean
    ){
        // flagがfalseがlistのupdate、trueがtaskのupdate
        val tasksBefore = if (!flag){
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 0, task, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 1, task, aStr = null, flag = false)
        }
        Log.d("update_b", tasksBefore!!)

        var tasksAfter = ""
        for ((index, value) in tasksBefore.split(" ").withIndex()){
            tasksAfter += when (index) {
                position -> {
                    if (index == lastPosition){
                        value.replace(value, aStr)
                    } else
                        value.replace(value, "$aStr ")
                }
                lastPosition -> value
                else -> "$value "
            }
        }

        Log.d("update_a", tasksAfter)
        /*
         tasksAfter...置換後のファイルの中身
         task...oldFileName
         aTask...newFileName
         */
        if (!flag){
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 4, task, aStr, flag = true)
        }else{
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, task, aStr = null, flag = true)
        }
    }

    fun todoTask(): MutableLiveData<MutableList<MutableMap<String, String>>>{
        return _todoTask
    }

    fun todoList(): MutableLiveData<String>{
        return _todoList
    }

    fun setPosition(position: Int, size: Int){
        this.position = position
        this.lastPosition = size - 1
    }

    init {
        _todoList.value = ""
        _todoTask.value = mutableListOf()
        position = 0
        lastPosition = 0
    }
}