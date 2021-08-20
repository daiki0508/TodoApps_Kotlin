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
    private val _completeFlag = MutableLiveData<MutableMap<String, Boolean?>>().apply {
        MutableLiveData<MutableMap<String, Boolean?>>()
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
                firebaseStorageRepository.download(context, addViewModel = null, this, storage, auth, lists, flag)
            }
        }
    }

    fun delete(context: Context, storage: FirebaseStorage, auth: FirebaseAuth, position: Int, flag: Boolean){
        if (flag){
            firebaseStorageRepository.delete(storage, auth, task = null, flag)
        }else{
            val tasks = CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
            if (File("${context.filesDir}/task/${tasks!!.split(" ")[position]}/task").exists()){
                //TODO("未実装")
                firebaseStorageRepository.delete(storage, auth, tasks.split(" ")[position], flag)
            }
        }
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
        val tasks: String? = if (/*taskFile.exists() || */taskFile.length() != 0L){
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
        val listsBefore = CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        Log.d("update_b", listsBefore!!)
        // listから該当task名を削除
        var listsAfter = listsBefore!!.replace("${listsBefore.split(" ")[position]} ", "")
        if (listsBefore == listsAfter){
            listsAfter = listsBefore.replace(" ${listsBefore.split(" ")[position]}", "")
            if (listsBefore == listsAfter){
                // listの削除
                CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "", type = 6, task = null, aStr = null, flag = true)
            }else{
                // listのtask名を削除
                CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "", type = 7, task = listsBefore.split(" ")[position], aStr = null, flag = true)
            }
        }else{
            // listのtask名を削除
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), listsAfter, type = 7, task = listsBefore.split(" ")[position], aStr = null, flag = true)
        }
        if (File("${context.filesDir}/task/${listsBefore.split(" ")[position]}/task").exists()){
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), listsAfter, type = 5, task = listsBefore.split(" ")[position], aStr = null, flag = true)
        }
        Log.d("update_a", listsAfter)
    }

    fun completeFlag(): MutableLiveData<MutableMap<String, Boolean?>>{
        return _completeFlag
    }

    fun todoList(): MutableLiveData<MutableList<MutableMap<String, String>>>{
        return _todoList
    }

    fun setCompleteFlag(taskMap: MutableMap<String, Boolean?>){
        _completeFlag.value = taskMap
    }

    init {
        _completeFlag.value = mutableMapOf(
            "list_list" to null,
            "iv_aes_list" to null,
            "salt_list" to null,
            "task_task" to null,
            "iv_aes_task" to null,
            "salt_task" to null
        )
        _todoList.value = mutableListOf()
    }
}