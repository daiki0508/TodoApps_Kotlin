package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.annotation.SuppressLint
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

@SuppressLint("StaticFieldLeak")
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

    private var activity: Activity?
    private var storage: FirebaseStorage?
    private var auth: FirebaseAuth?
    //private var list: String

    fun upload(){
        firebaseStorageRepository.upload(activity!!, storage!!, auth!!, task = null, flag = false)
    }

    fun download(flag: Boolean){
        if (flag){
            firebaseStorageRepository.download(activity!!, addViewModel = null, this, storage!!, auth!!, tasks = null, flag)
        }else{
            if (File(activity?.filesDir, "list").exists()){
                val lists = CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag)
                firebaseStorageRepository.download(activity!!, addViewModel = null, this, storage!!, auth!!, lists, flag)
            }
        }
    }

    fun delete(position: Int, flag: Boolean){
        if (flag){
            firebaseStorageRepository.delete(storage!!, auth!!, task = null, flag)
        }else{
            val tasks = CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
            if (File("${activity?.filesDir}/task/${tasks!!.split(" ")[position]}/task").exists()){
                firebaseStorageRepository.delete(storage!!, auth!!, tasks.split(" ")[position], flag)
            }
        }
    }

    fun readPreference(list: String, keyName: String): Boolean{
        return preferenceRepository.read(activity!!, list, keyName)
    }

    fun deletePreference(list: String){
        preferenceRepository.delete(activity!!, list)
    }

    fun countUnCompleteTask(list: String): Int{
        var cnt = 0

        if (File("${activity?.filesDir}/task/$list/task").length() != 0L){
            val tasks = CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, null, flag = false)

            val todoTask: MutableList<MutableMap<String, String>> = mutableListOf()
            var todo: MutableMap<String, String>
            for (task in tasks?.split(" ")!!){
                Log.d("test", task)
                todo = mutableMapOf("task" to task)
                todoTask.add(todo)
            }

            for (item in todoTask){
                if (!readPreference(list, item["task"]!!)){
                    cnt++
                }
            }
        }
        return cnt
    }

    fun createView(){
        val lists = CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        Log.d("lists", lists!!)

        _todoList.value = createTodoContents(lists, "list")
    }

    fun getTask(list: String): MutableList<MutableMap<String, String>>{
        val taskFile = File("${activity?.filesDir}/task/$list/task")
        val tasks: String? = if (taskFile.length() != 0L){
            CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 2, list, aStr = null, flag = false)
        }

        return createTodoContents(tasks, keyName = "task")
    }

    private fun createTodoContents(contents: String?, keyName: String): MutableList<MutableMap<String, String>>{
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

    fun move(items: MutableList<MutableMap<String, String>>, fromPosition: Int, toPosition: Int){
        val toPositionItem = items[toPosition]["list"]
        val fromPositionItem = items[fromPosition]["list"]
        val lists = CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)

        Log.d("remove_b", lists!!)
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
        Log.d("remove_a", newLists)
        CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), newLists, type = 7, task = null, aStr = null, flag = true)

        upload()
    }

    fun listDelete(position: Int){
        val listsBefore = CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        Log.d("update_b", listsBefore!!)
        // listから該当task名を削除
        var listsAfter = listsBefore!!.replace("${listsBefore.split(" ")[position]} ", "")
        if (listsBefore == listsAfter){
            listsAfter = listsBefore.replace(" ${listsBefore.split(" ")[position]}", "")
            if (listsBefore == listsAfter){
                // listの削除
                CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "", type = 6, task = null, aStr = null, flag = true)
            }else{
                // listのtask名を削除
                CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "", type = 7, task = listsBefore.split(" ")[position], aStr = null, flag = true)
            }
        }else{
            // listのtask名を削除
            CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), listsAfter, type = 7, task = listsBefore.split(" ")[position], aStr = null, flag = true)
        }
        if (File("${activity?.filesDir}/task/${listsBefore.split(" ")[position]}/task").exists()){
            CryptClass().decrypt(activity!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "", type = 5, task = listsBefore.split(" ")[position], aStr = null, flag = true)
        }
        Log.d("update_a", listsAfter)
    }

    fun completeFlag(): MutableLiveData<MutableMap<String, Boolean?>>{
        return _completeFlag
    }

    fun todoList(): MutableLiveData<MutableList<MutableMap<String, String>>>{
        return _todoList
    }

    fun setInit(auth: FirebaseAuth, context: Activity, storage: FirebaseStorage){
        this.auth = auth
        this.activity = context
        this.storage = storage
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
        auth = null
        activity = null
        storage = null
    }
}