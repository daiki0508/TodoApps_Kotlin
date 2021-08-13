package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageUploadRepositoryClient

class TodoViewModel(
    private val firebaseStorageRepository: FirebaseStorageUploadRepositoryClient
): ViewModel() {
    fun upload(context: Context, storage: FirebaseStorage, auth: FirebaseAuth){
        firebaseStorageRepository.upload(context, storage, auth)
    }
}