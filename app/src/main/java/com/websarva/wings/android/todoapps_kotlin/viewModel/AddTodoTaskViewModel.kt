package com.websarva.wings.android.todoapps_kotlin.viewModel

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

    fun upload(context: Context, storage: FirebaseStorage, auth: FirebaseAuth, task: String?, flag: Boolean){
        firebaseStorageRepository.upload(context, storage, auth, task, flag)
    }

    fun download(context: Context, storage: FirebaseStorage, auth: FirebaseAuth, task: String){
        firebaseStorageRepository.download(context, this, todoViewModel = null, storage, auth, task, flag = false)
    }

    fun delete(storage: FirebaseStorage, auth: FirebaseAuth, task: String){
        firebaseStorageRepository.delete(storage, auth, task, flag = false)
    }

    fun writePreference(activity: Activity, task: String, keyName: String, checkFlag: Boolean){
        preferenceRepository.write(activity, task, keyName, checkFlag)
    }

    fun readPreference(activity: Activity, task: String, keyName: String): Boolean{
        return preferenceRepository.read(activity, task, keyName)
    }

    fun deletePreference(activity: Activity, list: String){
        preferenceRepository.delete(activity, list)
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

    fun update(
        context: Context,
        auth: FirebaseAuth,
        task: String,
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
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 0, task, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 1, task, aStr = null, flag = false)
        }
        Log.d("update_b", tasksBefore!!)

        val tasksAfter = tasksBefore.replace(tasksBefore.split(" ")[position], aStr)

        Log.d("update_a", tasksAfter)

        if (!flag){
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 4, task, aStr, flag = true)
        }else{
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, task, aStr = null, flag = true)
        }
    }

    fun taskDelete(context: Context, auth: FirebaseAuth, task: String, position: Int){
        val tasksBefore = CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 1, task, aStr = null, flag = false)
        Log.d("update_b", tasksBefore!!)
        // taskファイルから該当タスクを削除
        var tasksAfter = tasksBefore!!.replace("${tasksBefore.split(" ")[position]} ", "")
        if (tasksBefore == tasksAfter){
            tasksAfter = tasksBefore.replace(" ${tasksBefore.split(" ")[position]}", "")
            if (tasksBefore == tasksAfter){
                // listから該当task名を削除
                CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), "", type = 5, task, aStr = null, flag = true)
            }else{
                CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, task, aStr = null, flag = true)
            }
        }else{
            CryptClass().decrypt(context, "${auth.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, task, aStr = null, flag = true)
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

    init {
        _completeFlag.value = mutableMapOf("task_task" to false, "iv_aes_task" to false, "salt_task" to false)
        _todoTask.value = mutableListOf()
        _updateName.value = ""
        position = 0
    }
}