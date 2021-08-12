package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.app.Activity
import android.util.Log
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseTopRepositoryClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val firebaseTopRepository: FirebaseTopRepositoryClient
): ViewModel() {
    fun firebaseAuthWithGoogle(activity: Activity, auth: FirebaseAuth, idToken: String): Task<AuthResult>{
        return firebaseTopRepository.firebaseAuthWithGoogle(activity, auth, idToken)
    }
}