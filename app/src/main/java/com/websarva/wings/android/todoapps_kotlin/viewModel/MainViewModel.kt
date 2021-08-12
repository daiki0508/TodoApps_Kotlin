package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseTopRepositoryClient

class MainViewModel(
    private val firebaseTopRepository: FirebaseTopRepositoryClient
): ViewModel() {
    fun firebaseAuthWithGoogle(activity: Activity, auth: FirebaseAuth, idToken: String): Boolean{
        return firebaseTopRepository.firebaseAuthWithGoogle(activity, auth, idToken)
    }
}