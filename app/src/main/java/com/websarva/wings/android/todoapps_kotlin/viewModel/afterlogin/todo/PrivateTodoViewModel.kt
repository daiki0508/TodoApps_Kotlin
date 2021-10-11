package com.websarva.wings.android.todoapps_kotlin.viewModel.afterlogin.todo

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.overlay.BalloonOverlayAnimation
import com.skydoves.balloon.overlay.BalloonOverlayRect
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.model.IntentBundle
import com.websarva.wings.android.todoapps_kotlin.model.PreferenceBalloon
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.OffLineRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.PreferenceBalloonRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.PreferenceRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.DeleteAllEvent
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.TodoFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PrivateTodoViewModel(
    private val preferenceBalloonRepository: PreferenceBalloonRepositoryClient,
    private val firebaseStorageRepository: FirebaseStorageRepositoryClient,
    private val preferenceRepository: PreferenceRepositoryClient,
    private val offLineRepository: OffLineRepositoryClient,
    application: Application
) : AndroidViewModel(application) {
    private val _context = MutableLiveData<Context>()
    private val _storage = MutableLiveData<FirebaseStorage>().apply {
        MutableLiveData<FirebaseStorage>()
    }
    private val _auth = MutableLiveData<FirebaseAuth>().apply {
        MutableLiveData<FirebaseAuth>()
    }
    private val _networkStatus = MutableLiveData<Boolean>()
    private val _bundle = MutableLiveData<Bundle>()
    private val _list = MutableLiveData<String>()
    private val _fabBalloon = MutableLiveData<Balloon>()
    private val _balloonComplete = MutableLiveData<Boolean>()
    private val _contentBalloon0 = MutableLiveData<Balloon>()
    private val _contentBalloon1 = MutableLiveData<Balloon>()
    private val _deleteAllComplete = MutableLiveData<DeleteAllEvent<Boolean>>()
    val deleteAllComplete: LiveData<DeleteAllEvent<Boolean>> = _deleteAllComplete

    @UiThread
    fun showBalloonFlag(fragment: TodoFragment){
        viewModelScope.launch {
            if (showBalloonFlagBackGround()){
                // 初回
                setBalloon(fragment)
            }else{
                // 2回目以降
                setBalloonComplete()
            }
        }
    }
    @WorkerThread
    private suspend fun showBalloonFlagBackGround(): Boolean = withContext(Dispatchers.IO){
        preferenceBalloonRepository.read(_context.value!!, flag = true).retBalloon
    }
    private fun setBalloon(fragment: TodoFragment){
        _fabBalloon.value = createBalloon(_context.value!!.getString(R.string.fab_balloon), fragment)
        _contentBalloon0.value = createBalloon(_context.value!!.getString(R.string.content_list_balloon0), fragment)
        _contentBalloon1.value = createBalloon(_context.value!!.getString(R.string.content_list_balloon1), fragment)
    }
    private fun createBalloon(text: String, fragment: TodoFragment): Balloon{
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
        preferenceBalloonRepository.save(_context.value!!, flag = true)
    }
    fun connectingStatus(): NetworkCapabilities? {
        val connectivityManager =
            getApplication<Application>().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 戻り値がnullでなければ、ネットワークに接続されている
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    }
    @UiThread
    fun download(flag: Boolean, todoViewModel: TodoViewModel){
        viewModelScope.launch {
            downloadBackGround(flag, todoViewModel)
        }
    }
    @WorkerThread
    private suspend fun downloadBackGround(flag: Boolean, todoViewModel: TodoViewModel) = withContext(Dispatchers.IO){
        if (flag){
            firebaseStorageRepository.download(_context.value!!, addViewModel = null, todoViewModel, _storage.value!!, _auth.value!!, tasks = null, flag)
        }else{
            if (File(_context.value?.filesDir, FileName().list).exists() && File(_context.value?.filesDir, FileName().list).length() != 0L){
                val lists = CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag)
                firebaseStorageRepository.download(_context.value!!, addViewModel = null, todoViewModel, _storage.value!!, _auth.value!!, lists, flag)
            }else{
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
    fun deletePreference(list: String){
        preferenceRepository.delete(_context.value!!, list)
    }
    @UiThread
    fun deleteAll(){
        viewModelScope.launch {
            deleteAllBackGround()
            _deleteAllComplete.value = DeleteAllEvent(true)
        }
    }
    @WorkerThread
    private suspend fun deleteAllBackGround() = withContext(Dispatchers.IO){
        if (_auth.value?.currentUser!!.uid != offLineRepository.read(_context.value!!)){
            if (File(_context.value?.filesDir, FileName().list).length() != 0L){
                File("${_context.value?.filesDir}/${FileName().list}").deleteRecursively()
                File("${_context.value?.filesDir}/${FileName().iv_aes}").deleteRecursively()
                File("${_context.value?.filesDir}/${FileName().salt}").deleteRecursively()

                File("${_context.value?.filesDir}/task").deleteRecursively()
            }
        }
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
    fun getTask(list: String, todoViewModel: TodoViewModel): MutableList<MutableMap<String, String>>{
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

        return todoViewModel.createTodoContents(tasks, keyName = FileName().task)
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
                    CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), listsAfter, type = 7, task = listsBefore.split(" ")[position], aStr = null, flag = true)
                }else{
                    CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), listsAfter, type = 7, task = listsBefore.split(" ")[position], aStr = null, flag = true)
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
            Log.d("exists", "called")
            if (connectingStatus() != null){
                CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "", type = 5, task = listsBefore.split(" ")[position], aStr = null, flag = true)
            }else{
                CryptClass().decrypt(_context.value!!, offLineRepository.read(_context.value!!)!!.toCharArray(), "", type = 5, task = listsBefore.split(" ")[position], aStr = null, flag = true)
            }
        }
        //Log.d("after", listsAfter)
        //Log.d("update_a", "after: ${CryptClass().decrypt(_context.value!!, "${_auth.value?.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)}")
    }

    fun bundle(): MutableLiveData<Bundle>{
        return _bundle
    }
    fun list(): MutableLiveData<String>{
        return _list
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
    fun balloonComplete(): MutableLiveData<Boolean>{
        return _balloonComplete
    }

    fun setInit(auth: FirebaseAuth?, storage: FirebaseStorage?, networkStatus: Boolean){
        _auth.value = auth
        _storage.value = storage
        _networkStatus.value = networkStatus
    }
    fun setBundle(list: String, position: Int){
        _bundle.value = Bundle().apply {
            this.putString(FileName().list, list)
            this.putInt(IntentBundle.Position.name, position)
        }
    }
    fun setList(list: String){
        _list.value = list
    }
    fun setBalloonComplete(){
        _balloonComplete.value = true
    }

    init {
        _context.value = getApplication<Application>().applicationContext
        _balloonComplete.value = false
    }
}