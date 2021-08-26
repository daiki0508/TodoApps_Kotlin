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
    private val preferenceRepository: PreferenceRepositoryClient,
    private val offLineRepository: OffLineRepositoryClient
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
    private var networkStatus: Boolean?

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

    fun countUnCompleteTask(items: MutableList<MutableMap<String, String>>?, list: String?): Int{
        var cnt = 0

        // trueがNavigationDrawer用
        if (list != null){
            if (File("${context?.filesDir}/task/$list/task").length() != 0L){
                val tasks: String? = if (networkStatus == true){
                    CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, null, flag = false)
                }else{
                    CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), "",type = 1, list, null, flag = false)
                }

                val todoTask: MutableList<MutableMap<String, String>> = mutableListOf()
                var todo: MutableMap<String, String>
                for (task in tasks?.split(" ")!!){
                    Log.d("test", task)
                    todo = mutableMapOf("task" to task)
                    todoTask.add(todo)
                }

                for (task in todoTask){
                    if (!preferenceRepository.read(context!!, list, task["task"]!!)){
                        cnt++
                    }
                }
            }
        }else{
            for (item in items!!){
                if (!readPreference(item["task"]!!)){
                    cnt++
                }
            }
        }
        return cnt
    }

    fun createView(){
        // ネットワークの接続状況によって処理を分岐
        val tasks: String? = if (networkStatus == true){
            CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, null, flag = false)
        }else{
            CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), "",type = 1, list, null, flag = false)
        }

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
        // ネットワークの接続状況によって処理を分岐
        val lists: String? = if (networkStatus == true){
            CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        }

        val todoList: MutableList<MutableMap<String, String>> = mutableListOf()
        var todo: MutableMap<String, String>
        for (list in lists!!.split(" ")){
            todo = mutableMapOf("list" to list)
            todoList.add(todo)
        }
        return todoList
    }

    fun add(list: String){
        if (networkStatus == true){
            CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), list, type = 1, this.list, aStr = null, flag = true)
        }else{
            CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), list, type = 1, this.list, aStr = null, flag = true)
        }
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
            // ネットワークの接続状況によって処理を分岐
            if (networkStatus == true){
                CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 0, list, aStr = null, flag = false)
            }else{
                CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), "",type = 0, list, aStr = null, flag = false)
            }
        }else{
            // ネットワークの接続状況によって処理を分岐
            if (networkStatus == true){
                CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, aStr = null, flag = false)
            }else{
                CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), "",type = 1, list, aStr = null, flag = false)
            }
        }
        Log.d("update_b", tasksBefore!!)

        val tasksAfter = tasksBefore.replace(tasksBefore.split(" ")[position], aStr)

        Log.d("update_a", tasksAfter)

        if (!flag){
            // ネットワークの接続状況によって処理を分岐
            if (networkStatus == true){
                CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 4, list, aStr, flag = true)
            }else{
                CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), tasksAfter, type = 4, list, aStr, flag = true)
            }
        }else{
            // ネットワークの接続状況によって処理を分岐
            if (networkStatus == true){
                CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, list, aStr = null, flag = true)
            }else{
                CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), tasksAfter, type = 3, list, aStr = null, flag = true)
            }
        }
    }

    fun move(items: MutableList<MutableMap<String, String>>, fromPosition: Int, toPosition: Int, flag: Boolean){
        // trueがNavigationDrawer
        val toPositionItem = if (flag){
            items[toPosition]["list"]
        }else{
            items[toPosition]["task"]
        }
        val fromPositionItem = if (flag){
            items[fromPosition]["list"]
        }else{
            items[fromPosition]["task"]
        }
        val contents = if (flag){
            // ネットワークの接続状態によって処理を分岐
            if (networkStatus == true){
                CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
            }else{
                CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
            }
        }else{
            // ネットワークの接続状態によって処理を分岐
            if (networkStatus == true){
                CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, aStr = null, flag = false)
            }else{
                CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), "",type = 1, list, aStr = null, flag = false)
            }
        }

        //Log.d("remove_b", tasks!!)
        var newContents = ""
        for ((i, value) in contents!!.split(" ").withIndex()){
            when (i) {
                toPosition -> {
                    newContents += if (i.plus(1) == items.size){
                        fromPositionItem
                    }else{
                        "$fromPositionItem "
                    }
                }
                fromPosition -> {
                    newContents += if (i.plus(1) == items.size){
                        toPositionItem
                    }else{
                        "$toPositionItem "
                    }
                }
                else -> {
                    newContents += if (i.plus(1) == items.size){
                        value
                    }else{
                        "$value "
                    }
                }
            }
        }
        //Log.d("remove_a", newTasks)
        if (flag){
            // ネットワークの接続状態によって処理を分岐
            if (networkStatus == true){
                CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), newContents, type = 7, task = null, aStr = null, flag = true)
                upload(flag = false)
            }else{
                CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), newContents, type = 7, task = null, aStr = null, flag = true)
            }
        }else{
            // ネットワークの接続状態によって処理を分岐
            if (networkStatus == true){
                CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), newContents, type = 3, list, aStr = null, flag = true)
                upload(flag = true)
            }else{
                CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), newContents, type = 3, list, aStr = null, flag = true)
            }
        }
    }

    fun taskDelete(position: Int){
        // ネットワークの接続状態によって処理を分岐
        val tasksBefore: String? = if (networkStatus == true){
            CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), "",type = 1, list, aStr = null, flag = false)
        }
        Log.d("update_b", tasksBefore!!)
        // taskファイルから該当タスクを削除
        var tasksAfter = tasksBefore!!.replace("${tasksBefore.split(" ")[position]} ", "")
        if (tasksBefore == tasksAfter){
            tasksAfter = tasksBefore.replace(" ${tasksBefore.split(" ")[position]}", "")
            if (tasksBefore == tasksAfter){
                /*
                 listから該当task名を削除
                 ネットワークの接続状態によって処理を分岐
                 */
                if (networkStatus == true){
                    CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), "", type = 5, list, aStr = null, flag = true)
                }else{
                    CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), "", type = 5, list, aStr = null, flag = true)
                }
            }else{
                // ネットワークの接続状態によって処理を分岐
                if (networkStatus == true){
                    CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, list, aStr = null, flag = true)
                }else{
                    CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), tasksAfter, type = 3, list, aStr = null, flag = true)
                }
            }
        }else{
            if (networkStatus == true){
                CryptClass().decrypt(context!!, "${auth?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, list, aStr = null, flag = true)
            }else{
                CryptClass().decrypt(context!!, offLineRepository.read(context!!)!!.toCharArray(), tasksAfter, type = 3, list, aStr = null, flag = true)
            }
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

    fun setInit(list: String, auth: FirebaseAuth?, context: Activity, storage: FirebaseStorage?, networkStatus: Boolean){
        this.list = list
        this.auth = auth
        this.context = context
        this.storage = storage
        this.networkStatus = networkStatus
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
        networkStatus = null
    }
}