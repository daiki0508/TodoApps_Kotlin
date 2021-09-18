package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.model.DownloadStatus
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.OffLineRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.PreferenceRepositoryClient
import kotlinx.coroutines.*
import java.io.File

class TodoViewModel(
    private val firebaseStorageRepository: FirebaseStorageRepositoryClient,
    private val preferenceRepository: PreferenceRepositoryClient,
    private val offLineRepository: OffLineRepositoryClient,
    application: Application
): AndroidViewModel(application) {
    private val _context = MutableLiveData<Context>().apply {
        MutableLiveData<Context>()
    }
    private val _todoList = MutableLiveData<MutableList<MutableMap<String, String>>>().apply {
        MutableLiveData<MutableList<MutableMap<String, String>>>()
    }
    private val _completeFlag = MutableLiveData<MutableMap<String, Boolean?>>().apply {
        MutableLiveData<MutableMap<String, Boolean?>>()
    }
    private val _storage = MutableLiveData<FirebaseStorage>().apply {
        MutableLiveData<FirebaseStorage>()
    }
    private val _auth = MutableLiveData<FirebaseAuth>().apply {
        MutableLiveData<FirebaseAuth>()
    }
    private val _networkStatus = MutableLiveData<Boolean>().apply {
        MutableLiveData<Boolean>()
    }

    fun connectingStatus(): NetworkCapabilities? {
        val connectivityManager =
            getApplication<Application>().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 戻り値がnullでなければ、ネットワークに接続されている
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    }

    fun upload(){
        firebaseStorageRepository.upload(_context.value!!, _storage.value!!, _auth.value!!, task = null, flag = false)
    }

    fun download(flag: Boolean){
        if (flag){
            firebaseStorageRepository.download(_context.value!!, addViewModel = null, this, _storage.value!!, _auth.value!!, tasks = null, flag)
        }else{
            if (File(_context.value?.filesDir, FileName().list).exists()){
                val lists = CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag)
                firebaseStorageRepository.download(_context.value!!, addViewModel = null, this, _storage.value!!, _auth.value!!, lists, flag)
            }
        }
    }

    fun delete(position: Int, flag: Boolean){
        if (flag){
            firebaseStorageRepository.delete(_storage.value!!, _auth.value!!, task = null, flag)
        }else{
            val tasks = CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
            if (File("${_context.value?.filesDir}/task/${tasks!!.split(" ")[position]}/${FileName().task}").exists()){
                firebaseStorageRepository.delete(_storage.value!!, _auth.value!!, tasks.split(" ")[position], flag)
            }
        }
    }

    fun readPreference(list: String, keyName: String): Boolean{
        return preferenceRepository.read(_context.value!!, list, keyName)
    }

    fun deletePreference(list: String){
        preferenceRepository.delete(_context.value!!, list)
    }

    fun deleteAll(){
        if (_auth.value?.currentUser!!.uid != offLineRepository.read(_context.value!!)){
            if (File(_context.value?.filesDir, FileName().list).length() != 0L){
                File("${_context.value?.filesDir}/${FileName().list}").deleteRecursively()
                File("${_context.value?.filesDir}/${FileName().iv_aes}").deleteRecursively()
                File("${_context.value?.filesDir}/${FileName().salt}").deleteRecursively()

                File("${_context.value?.filesDir}/task").deleteRecursively()
            }
        }
    }

    fun countUnCompleteTask(list: String): Int{
        var cnt = 0

        if (File("${_context.value?.filesDir}/task/$list/${FileName().task}").length() != 0L){
            // ネットワーク接続状態によって処理を分岐
            val tasks: String? = if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, null, flag = false)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 1, list, null, flag = false)
            }

            val todoTask: MutableList<MutableMap<String, String>> = mutableListOf()
            var todo: MutableMap<String, String>
            for (task in tasks?.split(" ")!!){
                //Log.d("test", task)
                todo = mutableMapOf(FileName().task to task)
                todoTask.add(todo)
            }

            for (item in todoTask){
                if (!readPreference(list, item[FileName().task]!!)){
                    cnt++
                }
            }
        }
        return cnt
    }

    fun update(list: String){
        if (_networkStatus.value == true){
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), list, type = 0, task = null, aStr = null, flag = true)
            }else{
                if (!File("${_context.value?.filesDir?.parent}/shared_prefs/offline.xml").exists()){
                    offLineRepository.write(_context.value!!)
                }
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), list, type = 0, task = null, aStr = null, flag = true)
            }
        }else{
            if (!File("${_context.value?.filesDir?.parent}/shared_prefs/offline.xml").exists()){
                offLineRepository.write(_context.value!!)
            }
            CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), list, type = 0, task = null, aStr = null, flag = true)
        }
    }

    fun createView(){
        // ネットワークの接続状況によって処理を分割
        val lists: String? = if (_networkStatus.value == true){
            offLineRepository.online(_context.value!!, _auth.value!!)
            CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        }

        //Log.d("lists", lists!!)
        _todoList.value = createTodoContents(lists, FileName().list)
    }

    fun getTask(list: String): MutableList<MutableMap<String, String>>{
        val taskFile = File("${_context.value?.filesDir}/task/$list/${FileName().task}")
        val tasks: String? = if (taskFile.length() != 0L){
            // ネットワーク接続状態によって処理を分岐
            if (_networkStatus.value == true){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 1, list, aStr = null, flag = false)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 1, list, aStr = null, flag = false)
            }
        }else{
            if (_networkStatus.value == true){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 2, list, aStr = null, flag = false)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 2, list, aStr = null, flag = false)
            }
        }

        return createTodoContents(tasks, keyName = FileName().task)
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
        val toPositionItem = items[toPosition][FileName().list]
        val fromPositionItem = items[fromPosition][FileName().list]
        // ネットワーク接続状態によって処理を分岐
        val lists: String? = if (connectingStatus() != null){
            CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        }

        //Log.d("remove_b", lists!!)
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
        //Log.d("remove_a", newLists)
        // ネットワーク接続状態によって処理を分岐
        if (connectingStatus() != null){
            CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), newLists, type = 7, task = null, aStr = null, flag = true)
            upload()
        }else{
            CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), newLists, type = 7, task = null, aStr = null, flag = true)
        }
    }

    fun listDelete(position: Int){
        // ネットワーク接続状態によって処理を分岐
        val listsBefore: String? = if (connectingStatus() != null){
            CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
        }
        //Log.d("update_b", listsBefore!!)
        // listから該当task名を削除
        var listsAfter = listsBefore!!.replace("${listsBefore.split(" ")[position]} ", "")
        if (listsBefore == listsAfter){
            listsAfter = listsBefore.replace(" ${listsBefore.split(" ")[position]}", "")
            if (listsBefore == listsAfter){
                /*
                 listの削除
                 ネットワーク接続状態によって処理を分岐
                 */
                if (connectingStatus() != null){
                    CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "", type = 6, task = null, aStr = null, flag = true)
                }else{
                    CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "", type = 6, task = null, aStr = null, flag = true)
                }
            }else{
                /*
                 listのtask名を削除
                 ネットワーク接続状態によって処理を分岐
                 */
                if (connectingStatus() != null){
                    CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "", type = 7, task = listsBefore.split(" ")[position], aStr = null, flag = true)
                }else{
                    CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "", type = 7, task = listsBefore.split(" ")[position], aStr = null, flag = true)
                }
            }
        }else{
            /*
             listのtask名を削除
             ネットワーク接続状態によって処理を分岐
             */
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), listsAfter, type = 7, task = listsBefore.split(" ")[position], aStr = null, flag = true)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), listsAfter, type = 7, task = listsBefore.split(" ")[position], aStr = null, flag = true)
            }
        }
        if (File("${_context.value?.filesDir}/task/${listsBefore.split(" ")[position]}/${FileName().task}").exists()){
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "", type = 5, task = listsBefore.split(" ")[position], aStr = null, flag = true)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "", type = 5, task = listsBefore.split(" ")[position], aStr = null, flag = true)
            }
        }
        //Log.d("update_a", CryptClass().decrypt(activity!!, offLineRepository.read(activity!!)!!.toCharArray(), "",type = 0, task = null, aStr = null, flag = false)!!)
    }

    fun completeFlag(): MutableLiveData<MutableMap<String, Boolean?>>{
        return _completeFlag
    }

    fun todoList(): MutableLiveData<MutableList<MutableMap<String, String>>>{
        return _todoList
    }

    fun setInit(auth: FirebaseAuth?, storage: FirebaseStorage?, networkStatus: Boolean){
        _auth.value = auth
        _storage.value = storage
        _networkStatus.value = networkStatus
    }

    fun setCompleteFlag(taskMap: MutableMap<String, Boolean?>){
        _completeFlag.value = taskMap
    }

    init {
        _context.value = getApplication<Application>().applicationContext
        _completeFlag.value = mutableMapOf(
            DownloadStatus().list to null,
            DownloadStatus().iv_aes_list to null,
            DownloadStatus().salt_list to null,
            DownloadStatus().task to null,
            DownloadStatus().iv_aes_task to null,
            DownloadStatus().salt_task to null
        )
        _todoList.value = mutableListOf()
    }
}