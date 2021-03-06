package com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.add

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    @UiThread
    fun showBalloonFlag(fragment: AddTodoTaskFragment){
        viewModelScope.launch {
            if (showBalloonFlagBackGround()){
                // ??????
                setBalloon(fragment)
            }else{
                // 2????????????
                setBalloonComplete()
            }
        }
    }
    @WorkerThread
    private suspend fun showBalloonFlagBackGround(): Boolean = withContext(Dispatchers.IO){
        preferenceBalloonRepository.read(_context.value!!, flag = false).retBalloon
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
    @UiThread
    fun save(){
        viewModelScope.launch {
            saveBackGround()
        }
    }
    @WorkerThread
    private suspend fun saveBackGround() = withContext(Dispatchers.IO){
        preferenceBalloonRepository.save(_context.value!!, flag = false)
    }
    fun connectingStatus(): NetworkCapabilities? {
        val connectivityManager =
            _context.value?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // ????????????null????????????????????????????????????????????????????????????
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    }
    @UiThread
    fun upload(flag: Boolean){
        viewModelScope.launch {
            uploadBackGround(flag)
        }
    }
    @WorkerThread
    private suspend fun uploadBackGround(flag: Boolean) = withContext(Dispatchers.IO){
        firebaseStorageRepository.upload(_context.value!!, _storage.value!!, _auth.value!!, _list.value, flag)
    }
    @UiThread
    fun delete(){
        viewModelScope.launch {
            deleteBackGround()
        }
    }
    @WorkerThread
    private suspend fun deleteBackGround() = withContext(Dispatchers.IO){
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
    fun countUnCompleteTask(items: MutableList<MutableMap<String, String>>?): Int{
        var cnt = 0

        // list???null??????????????????NavigationDrawer???
            for (item in items!!) {
                if (!readPreference(item[FileName().task]!!)) {
                    cnt++
                }
            }
        return cnt
    }
    @UiThread
    fun createView(){
        viewModelScope.launch {
            _todoTask.value = createViewBackGround()
        }
    }
    @WorkerThread
    private suspend fun createViewBackGround(): MutableList<MutableMap<String, String>> = withContext(Dispatchers.IO){
        // ????????????????????????????????????????????????????????????
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

        // ?????????
        todoTask
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
         flag???false???list???update???true???task???update
         tasksAfter...?????????????????????????????????
         task...oldFileName
         aStr...list???update??????newFileName, task???update???????????????task???
         */
        val tasksBefore = if (!flag){
            // ????????????????????????????????????????????????????????????
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, _list.value, aStr = null, flag = false)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 0, _list.value, aStr = null, flag = false)
            }
        }else{
            // ????????????????????????????????????????????????????????????
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
            // ????????????????????????????????????????????????????????????
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 4, _list.value, aStr, flag = true)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), tasksAfter, type = 4, _list.value, aStr, flag = true)
            }
        }else{
            // ????????????????????????????????????????????????????????????
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), tasksAfter, type = 3, _list.value, aStr = null, flag = true)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), tasksAfter, type = 3, _list.value, aStr = null, flag = true)
            }
        }
    }
    fun move(items: MutableList<MutableMap<String, String>>, fromPosition: Int, toPosition: Int, flag: Boolean){
        // true???NavigationDrawer
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
            // ????????????????????????????????????????????????????????????
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
            }
        }else{
            // ????????????????????????????????????????????????????????????
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
            // ????????????????????????????????????????????????????????????
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), newContents, type = 7, task = null, aStr = null, flag = true)
                upload(flag = false)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), newContents, type = 7, task = null, aStr = null, flag = true)
            }
        }else{
            // ????????????????????????????????????????????????????????????
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), newContents, type = 3, _list.value, aStr = null, flag = true)
                upload(flag = true)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), newContents, type = 3, _list.value, aStr = null, flag = true)
            }
        }
    }
    fun taskDelete(position: Int){
        // ????????????????????????????????????????????????????????????
        val tasksBefore: String? = if (connectingStatus() != null){
            CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 1, _list.value, aStr = null, flag = false)
        }else{
            CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "",type = 1, _list.value, aStr = null, flag = false)
        }
        //Log.d("update_b", tasksBefore!!)
        // task??????????????????????????????????????????
        var tasksAfter = tasksBefore!!.replace("${tasksBefore.split(" ")[position]} ", "")
        if (tasksBefore == tasksAfter){
            tasksAfter = tasksBefore.replace(" ${tasksBefore.split(" ")[position]}", "")
            if (tasksBefore == tasksAfter){
                /*
                 list????????????task????????????
                 ????????????????????????????????????????????????????????????
                 */
                if (connectingStatus() != null){
                    CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "", type = 5, _list.value, aStr = null, flag = true)
                }else{
                    CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "", type = 5, _list.value, aStr = null, flag = true)
                }
            }else{
                // ????????????????????????????????????????????????????????????
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