package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.CryptClass
import com.websarva.wings.android.todoapps_kotlin.model.DownloadStatus
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseTopRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.OffLineRepositoryClient
import java.io.File

class SettingsViewModel(
    private val firebaseTopRepository: FirebaseTopRepositoryClient,
    private val offLineRepository: OffLineRepositoryClient,
    private val firebaseStorageRepository: FirebaseStorageRepositoryClient
): ViewModel() {
    private val _completeFlag = MutableLiveData<MutableMap<String, Boolean?>>().apply {
        MutableLiveData<MutableMap<String, Boolean?>>()
    }

    @SuppressLint("StaticFieldLeak")
    private var activity: Activity? = null

    fun connectingStatus(): NetworkCapabilities? {
        val connectivityManager =
            activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 戻り値がnullでなければ、ネットワークに接続されている
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    }

    fun firebaseAuthWithGoogle(auth: FirebaseAuth, idToken: String): Task<AuthResult> {
        return firebaseTopRepository.firebaseAuthWithGoogle(activity!!, auth, idToken)
    }

    fun backup(auth: FirebaseAuth, storage: FirebaseStorage){
        if (File(activity?.filesDir, FileName().list).length() != 0L){
            // onlineに変更されたことによってpassを永続的に変更している
            val lists = CryptClass().decrypt(activity!!, offLineRepository.read(activity!!)!!.toCharArray(), "",type = 0, task = null, aStr = null, flag = false)
            CryptClass().decrypt(activity!!, "${auth.currentUser!!.uid}0000".toCharArray(), lists!!, type = 7, task = null, aStr = null, flag = true)

            // listをFirebaseStorageにバックアップ
            upload(auth, storage, list = null, flag = false)

            for (list in lists.split(" ")){
                val tasks = CryptClass().decrypt(activity!!, offLineRepository.read(activity!!)!!.toCharArray(), "",type = 1, list, aStr = null, flag = false)
                CryptClass().decrypt(activity!!, "${auth.currentUser!!.uid}0000".toCharArray(), tasks!!, type = 3, list, aStr = null, flag = true)

                // taskをFirebaseStorageにバックアップ
                upload(auth, storage, list, flag = true)
            }
            // preferenceのpassを変更
            offLineRepository.online(activity!!, auth)
        }
    }

    private fun upload(auth: FirebaseAuth, storage: FirebaseStorage, list: String?, flag: Boolean){
        // trueがtaskのアップロード
        firebaseStorageRepository.upload(activity!!, storage, auth, list, flag)
    }

    fun restore(auth: FirebaseAuth, storage: FirebaseStorage, flag: Boolean){
        // trueがlistの処理
        if (flag){
            firebaseStorageRepository.restore(activity!!, this, auth, storage, list = null, flag)
        }else{
            if (File(activity?.filesDir, FileName().list).length() != 0L){
                val lists = CryptClass().decrypt(activity!!, "${auth.currentUser!!.uid}0000".toCharArray(), "",type = 0, task = null, aStr = null, flag = false)

                for (list in lists!!.split(" ")){
                    firebaseStorageRepository.restore(activity!!, this, auth, storage, list, flag)
                }
            }
            offLineRepository.online(activity!!, auth)
        }
    }

    fun setInit(activity: Activity){
        this.activity = activity
    }
    fun setFlag(taskMap: MutableMap<String, Boolean?>){
        _completeFlag.value = taskMap
    }

    fun completeFlag(): MutableLiveData<MutableMap<String, Boolean?>>{
        return _completeFlag
    }

    init {
        _completeFlag.value = mutableMapOf(
            DownloadStatus().list to null,
            DownloadStatus().iv_aes_list to null,
            DownloadStatus().salt_list to null,
        )
    }
}