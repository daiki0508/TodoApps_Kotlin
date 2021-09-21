package com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.add

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.overlay.BalloonOverlayAnimation
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.model.DownloadStatus
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.repository.*
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.add.AddTodoTaskFragment
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.TodoFragment
import java.io.File

class AddTodoTaskViewModel(
    private val preferenceBalloonRepository: PreferenceBalloonRepositoryClient,
    private val firebaseStorageRepository: FirebaseStorageRepositoryClient,
    private val preferenceRepository: PreferenceRepositoryClient,
    private val offLineRepository: OffLineRepositoryClient,
    application: Application
): AndroidViewModel(application) {
    private val _todoTask = MutableLiveData<MutableList<MutableMap<String, String>>>()
    private val _completeFlag = MutableLiveData<MutableMap<String, Boolean?>>()
    private val _updateName = MutableLiveData<String>()
    private val _context = MutableLiveData<Context>()
    private val _storage = MutableLiveData<FirebaseStorage>().apply {
        MutableLiveData<FirebaseStorage>()
    }
    private val _auth = MutableLiveData<FirebaseAuth>().apply {
        MutableLiveData<FirebaseAuth>()
    }
    private val _networkStatus = MutableLiveData<Boolean>()
    private val _position = MutableLiveData<Int>()
    private val _list = MutableLiveData<String>()
    private val _data = MutableLiveData<Bundle>()
    private val _fabBalloon = MutableLiveData<Balloon>()
    private val _contentBalloon0 = MutableLiveData<Balloon>()
    private val _contentBalloon1 = MutableLiveData<Balloon>()
    private val _contentBalloon2 = MutableLiveData<Balloon>()
    private val _balloonComplete = MutableLiveData<Boolean>()

    fun showBalloonFlag(fragment: AddTodoTaskFragment){
        if (preferenceBalloonRepository.read(_context.value!!, flag = false).retBalloon){
            // 初回
            setBalloon(fragment)
        }else{
            // 2回目以降
            setBalloonComplete()
        }
    }
    private fun setBalloon(fragment: AddTodoTaskFragment){
        _fabBalloon.value = createBalloon(_context.value!!.getString(R.string.fab_balloon_task), fragment)
        _contentBalloon0.value = createBalloon(_context.value!!.getString(R.string.content_task_balloon0), fragment)
        _contentBalloon1.value = createBalloon(_context.value!!.getString(R.string.content_task_balloon1), fragment)
        _contentBalloon2.value = createBalloon(_context.value!!.getString(R.string.content_task_balloon2), fragment)
    }
    private fun createBalloon(text: String, fragment: AddTodoTaskFragment): Balloon{
        return com.skydoves.balloon.createBalloon(_context.value!!) {
            setArrowSize(10)
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(65)
            setArrowPosition(0.7f)
            setCornerRadius(4f)
            setAlpha(0.9f)
            setText(text)
            setTextColorResource(R.color.white)
            //setTextIsHtml(true)
            //setIconDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile))
            setBackgroundColorResource(R.color.dodgerblue)
            //setOnBalloonClickListener(onBalloonClickListener)
            setBalloonAnimation(BalloonAnimation.OVERSHOOT)
            setIsVisibleOverlay(true)
            setOverlayColorResource(R.color.darkgray)
            setOverlayPadding(6f)
            setBalloonOverlayAnimation(BalloonOverlayAnimation.FADE)
            setDismissWhenOverlayClicked(false)
            //setOverlayShape(BalloonOverlayRect)
            setLifecycleOwner(fragment.viewLifecycleOwner)
        }
    }
    fun save(){
        preferenceBalloonRepository.save(_context.value!!, flag = false)
    }
    fun connectingStatus(): NetworkCapabilities? {
        val connectivityManager =
            _context.value?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 戻り値がnullでなければ、ネットワークに接続されている
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    }

    fun upload(flag: Boolean){
        firebaseStorageRepository.upload(_context.value!!, _storage.value!!, _auth.value!!, _list.value, flag)
    }

    fun delete(){
        firebaseStorageRepository.delete(_storage.value!!, _auth.value!!, _list.value, flag = false)
    }

    fun writePreference(keyName: String, checkFlag: Boolean){
        preferenceRepository.write(_context.value!!, _list.value!!, keyName, checkFlag)
    }

    fun readPreference(keyName: String): Boolean{
        return preferenceRepository.read(_context.value!!, _list.value!!, keyName)
    }

    fun deletePreference(list: String){
        preferenceRepository.delete(_context.value!!, list)
    }

    fun countUnCompleteTask(items: MutableList<MutableMap<String, String>>?, list: String?): Int{
        var cnt = 0

        // listがnullでなければ、NavigationDrawer用
        if (list != null){
            if (File("${_context.value?.filesDir}/task/$list/${FileName().task}").length() != 0L){
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

                for (task in todoTask){
                    if (!preferenceRepository.read(_context.value!!, list, task[FileName().task]!!)){
                        cnt++
                    }
                }
            }
        }else{
            for (item in items!!){
                if (!readPreference(item[FileName().task]!!)){
                    cnt++
                }
            }
        }
        return cnt
    }

    fun createView(){
        // ネットワークの接続状況によって処理を分岐
        val tasks: String? = if (connectingStatus() != null){
            CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 1, _list.value, null, flag = false)
        }else{
            CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 1, _list.value, null, flag = false)
        }

        val todoTask: MutableList<MutableMap<String, String>> = mutableListOf()
        var todo: MutableMap<String, String>
        for (list in tasks?.split(" ")!!){
            //Log.d("test", list)
            todo = mutableMapOf(FileName().task to list)
            todoTask.add(todo)
        }
        _todoTask.value = todoTask
    }

    fun add(list: String){
        if (connectingStatus() != null){
            CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), list, type = 1, _list.value, aStr = null, flag = true)
        }else{
            CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), list, type = 1, _list.value, aStr = null, flag = true)
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
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, _list.value, aStr = null, flag = false)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 0, _list.value, aStr = null, flag = false)
            }
        }else{
            // ネットワークの接続状況によって処理を分岐
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 1, _list.value, aStr = null, flag = false)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 1, _list.value, aStr = null, flag = false)
            }
        }
        //Log.d("update_b", tasksBefore!!)

        val tasksAfter = tasksBefore!!.replace(tasksBefore.split(" ")[_position.value!!], aStr)

        //Log.d("update_a", tasksAfter)

        if (!flag){
            // ネットワークの接続状況によって処理を分岐
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 4, _list.value, aStr, flag = true)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), tasksAfter, type = 4, _list.value, aStr, flag = true)
            }
        }else{
            // ネットワークの接続状況によって処理を分岐
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, _list.value, aStr = null, flag = true)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), tasksAfter, type = 3, _list.value, aStr = null, flag = true)
            }
        }
    }

    fun move(items: MutableList<MutableMap<String, String>>, fromPosition: Int, toPosition: Int, flag: Boolean){
        // trueがNavigationDrawer
        val toPositionItem = if (flag){
            items[toPosition][FileName().list]
        }else{
            items[toPosition][FileName().task]
        }
        val fromPositionItem = if (flag){
            items[fromPosition][FileName().list]
        }else{
            items[fromPosition][FileName().task]
        }
        val contents = if (flag){
            // ネットワークの接続状態によって処理を分岐
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
            }
        }else{
            // ネットワークの接続状態によって処理を分岐
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 1, _list.value, aStr = null, flag = false)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 1, _list.value, aStr = null, flag = false)
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
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), newContents, type = 7, task = null, aStr = null, flag = true)
                upload(flag = false)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), newContents, type = 7, task = null, aStr = null, flag = true)
            }
        }else{
            // ネットワークの接続状態によって処理を分岐
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), newContents, type = 3, _list.value, aStr = null, flag = true)
                upload(flag = true)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), newContents, type = 3, _list.value, aStr = null, flag = true)
            }
        }
    }

    fun taskDelete(position: Int){
        // ネットワークの接続状態によって処理を分岐
        val tasksBefore: String? = if (connectingStatus() != null){
            CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 1, _list.value, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 1, _list.value, aStr = null, flag = false)
        }
        //Log.d("update_b", tasksBefore!!)
        // taskファイルから該当タスクを削除
        var tasksAfter = tasksBefore!!.replace("${tasksBefore.split(" ")[position]} ", "")
        if (tasksBefore == tasksAfter){
            tasksAfter = tasksBefore.replace(" ${tasksBefore.split(" ")[position]}", "")
            if (tasksBefore == tasksAfter){
                /*
                 listから該当task名を削除
                 ネットワークの接続状態によって処理を分岐
                 */
                if (connectingStatus() != null){
                    CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "", type = 5, _list.value, aStr = null, flag = true)
                }else{
                    CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "", type = 5, _list.value, aStr = null, flag = true)
                }
            }else{
                // ネットワークの接続状態によって処理を分岐
                if (connectingStatus() != null){
                    CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, _list.value, aStr = null, flag = true)
                }else{
                    CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), tasksAfter, type = 3, _list.value, aStr = null, flag = true)
                }
            }
        }else{
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, _list.value, aStr = null, flag = true)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), tasksAfter, type = 3, _list.value, aStr = null, flag = true)
            }
        }
        //Log.d("update_a", tasksAfter)
    }

    fun completeFlag(): MutableLiveData<MutableMap<String, Boolean?>>{
        return _completeFlag
    }
    fun todoTask(): MutableLiveData<MutableList<MutableMap<String, String>>>{
        return _todoTask
    }
    fun data(): MutableLiveData<Bundle>{
        return _data
    }
    fun fabBalloon(): MutableLiveData<Balloon>{
        return _fabBalloon
    }
    fun contentBalloon0(): MutableLiveData<Balloon>{
        return _contentBalloon0
    }
    fun contentBalloon1(): MutableLiveData<Balloon>{
        return _contentBalloon1
    }
    fun contentBalloon2(): MutableLiveData<Balloon>{
        return _contentBalloon2
    }
    fun balloonComplete(): MutableLiveData<Boolean>{
        return _balloonComplete
    }

    fun setCompleteFlag(taskMap: MutableMap<String, Boolean?>){
        _completeFlag.value = taskMap
    }
    fun setPosition(position: Int){
        _position.value = position
    }
    fun setInit(list: String, auth: FirebaseAuth?, storage: FirebaseStorage?, networkStatus: Boolean){
        _list.value = list
        _auth.value = auth
        _storage.value = storage
        _networkStatus.value = networkStatus
    }
    fun setData(data: Bundle){
        _data.value = data
    }
    fun setBalloonComplete(){
        _balloonComplete.value = true
    }

    init {
        _context.value = getApplication<Application>().applicationContext
        _balloonComplete.value = false
        _completeFlag.value = mutableMapOf(
            DownloadStatus().task to false,
            DownloadStatus().iv_aes_task to false,
            DownloadStatus().salt_task to false)
        _todoTask.value = mutableListOf()
        _updateName.value = ""
    }
}