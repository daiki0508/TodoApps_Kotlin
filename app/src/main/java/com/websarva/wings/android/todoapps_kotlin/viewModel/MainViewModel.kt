package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.websarva.wings.android.todoapps_kotlin.repository.AppUpdateRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseTopRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.ui.main.MainActivity
import kotlinx.coroutines.launch

class MainViewModel(
    private val firebaseTopRepository: FirebaseTopRepositoryClient,
    private val appUpdateRepository: AppUpdateRepositoryClient
): ViewModel() {
    fun firebaseAuthWithGoogle(auth: FirebaseAuth, idToken: String): Task<AuthResult>{
        return firebaseTopRepository.firebaseAuthWithGoogle(auth, idToken)
    }

    fun connectingStatus(activity: Activity): NetworkCapabilities? {
        val connectivityManager =
            activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 戻り値がnullでなければ、ネットワークに接続されている
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    }

    fun appUpdate(activity: Activity){
        viewModelScope.launch {
            appUpdateRepository.appUpdate(activity)
        }
    }

    fun restartUpdate(activity: MainActivity){
        viewModelScope.launch {
            // updateがない、もしくは完了している場合はfalseを返す
            val updateFlag = appUpdateRepository.restartUpdate(activity)
            if (!updateFlag) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    Log.i("test", "CurrentUser")
                    activity.afterLoginIntent(flag = true)
                }
            }
        }
    }
}