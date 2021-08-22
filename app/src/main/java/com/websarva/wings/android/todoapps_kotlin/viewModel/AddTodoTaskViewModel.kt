package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.repository.*
import com.websarva.wings.android.todoapps_kotlin.ui.AddListDialog
import com.websarva.wings.android.todoapps_kotlin.ui.add.AddTodoTaskActivity
import java.io.File

class AddTodoTaskViewModel(
    private val firebaseStorageRepository: FirebaseStorageRepositoryClient,
    private val preferenceRepository: PreferenceRepositoryClient
): ViewModel() {
    private val _todoTask = MutableLiveData<MutableList<MutableMap<String, String>>>().apply {
        MutableLiveData<MutableList<MutableMap<String, String>>>()
    }
    private val _completeFlag = MutableLiveData<MutableMap<String, Boolean?>>().apply {
        MutableLiveData<MutableMap<String, Boolean?>>()
    }

    private val _updateName = MutableLiveData<String>().apply {
        MutableLiveData<String>()
    }

    private var position: Int
    private var list: String
    private var auth: FirebaseAuth?
    @SuppressLint("StaticFieldLeak")
    private var context: Activity?
    private var storage: FirebaseStorage?

    fun upload(flag: Boolean){
        firebaseStorageRepository.upload(context!!, storage!!, auth!!, list, flag)
    }

    fun download(){
        firebaseStorageRepository.download(context!!, this, todoViewModel = null, storage!!, auth!!, list, flag = false)
    }

    fun delete(){
        firebaseStorageRepository.delete(storage!!, auth!!, list, flag = false)
    }

    fun writePreference(keyName: String, checkFlag: Boolean){
        preferenceRepository.write(context!!, list, keyName, checkFlag)
    }

    fun readPreference(keyName: String): Boolean{
        return preferenceRepository.read(context!!, list, keyName)
    }

    fun deletePreference(list: String){
        preferenceRepository.delete(context!!, list)
    }

    fun countUnCompleteTask(items: MutableList<MutableMap<String, String>>): Int{
        var cnt = 0

        for (item in items){
          if (!readPreference(item["task"]!!)){
              cnt++
          }
        }
        return cnt
    }

    fun createView(){
        val tasks = CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, null, flag = false)

        val todoTask: MutableList<MutableMap<String, String>> = mutableListOf()
        var todo: MutableMap<String, String>
        for (list in tasks?.split(" ")!!){
            Log.d("test", list)
            todo = mutableMapOf("task" to list)
            todoTask.add(todo)
        }
        _todoTask.value = todoTask
    }

    fun getList(): MutableList<MutableMap<String, String>>{
        val lists = CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)

        val todoList: MutableList<MutableMap<String, String>> = mutableListOf()
        var todo: MutableMap<String, String>
        for (list in lists!!.split(" ")){
            todo = mutableMapOf("list" to list)
            todoList.add(todo)
        }
        return todoList
    }

    fun update(
        aStr: String,
        flag: Boolean
    ){
        /*
         flagがfalseがlistのupdate、trueがtaskのupdate
         tasksAfter...置換後のファイルの中身
         task...oldFileName
         aStr...listのupdate時はnewFileName, taskのupdate時は新しいtask名
         */
        val tasksBefore = if (!flag){
            CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 0, list, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, aStr = null, flag = false)
        }
        Log.d("update_b", tasksBefore!!)

        val tasksAfter = tasksBefore.replace(tasksBefore.split(" ")[position], aStr)

        Log.d("update_a", tasksAfter)

        if (!flag){
            CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 4, list, aStr, flag = true)
        }else{
            CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, list, aStr = null, flag = true)
        }
    }

    fun remove(items: MutableList<MutableMap<String, String>>, fromPosition: Int, toPosition: Int){
        val toPositionItem = items[toPosition]["task"]
        val fromPositionItem = items[fromPosition]["task"]
        val tasks = CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, aStr = null, flag = false)

        Log.d("remove_b", tasks!!)
        var newTasks = ""
        for ((i, value) in tasks!!.split(" ").withIndex()){
            when (i) {
                toPosition -> {
                    newTasks += if (i.plus(1) == items.size){
                        fromPositionItem
                    }else{
                        "$fromPositionItem "
                    }
                }
                fromPosition -> {
                    newTasks += if (i.plus(1) == items.size){
                        toPositionItem
                    }else{
                        "$toPositionItem "
                    }
                }
                else -> {
                    newTasks += if (i.plus(1) == items.size){
                        value
                    }else{
                        "$value "
                    }
                }
            }
        }
        Log.d("remove_a", newTasks)
        CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), newTasks, type = 3, list, aStr = null, flag = true)

        upload(flag = true)
    }

    fun taskDelete(position: Int){
        val tasksBefore = CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, aStr = null, flag = false)
        Log.d("update_b", tasksBefore!!)
        // taskファイルから該当タスクを削除
        var tasksAfter = tasksBefore!!.replace("${tasksBefore.split(" ")[position]} ", "")
        if (tasksBefore == tasksAfter){
            tasksAfter = tasksBefore.replace(" ${tasksBefore.split(" ")[position]}", "")
            if (tasksBefore == tasksAfter){
                // listから該当task名を削除
                CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "", type = 5, list, aStr = null, flag = true)
            }else{
                CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, list, aStr = null, flag = true)
            }
        }else{
            CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, list, aStr = null, flag = true)
        }
        Log.d("update_a", tasksAfter)
    }

    fun completeFlag(): MutableLiveData<MutableMap<String, Boolean?>>{
        return _completeFlag
    }

    fun todoTask(): MutableLiveData<MutableList<MutableMap<String, String>>>{
        return _todoTask
    }

    fun setCompleteFlag(taskMap: MutableMap<String, Boolean?>){
        _completeFlag.value = taskMap
    }

    fun setPosition(position: Int){
        this.position = position
    }

    fun setInit(list: String, auth: FirebaseAuth, context: Activity, storage: FirebaseStorage){
        this.list = list
        this.auth = auth
        this.context = context
        this.storage = storage
    }

    init {
        _completeFlag.value = mutableMapOf("task_task" to false, "iv_aes_task" to false, "salt_task" to false)
        _todoTask.value = mutableListOf()
        _updateName.value = ""
        position = 0
        list = ""
        auth = null
        context = null
        storage = null
    }
}