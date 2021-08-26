package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseTopRepositoryClient

class MainViewModel(
    private val firebaseTopRepository: FirebaseTopRepositoryClient
): ViewModel() {
    fun firebaseAuthWithGoogle(activity: Activity, auth: FirebaseAuth, idToken: String): Task<AuthResult>{
        return firebaseTopRepository.firebaseAuthWithGoogle(activity, auth, idToken)
    }

    fun connectingStatus(activity: Activity): NetworkCapabilities? {
        val connectivityManager =
            activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 戻り値がnullでなければ、ネットワークに接続されている
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    }
}