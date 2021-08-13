package com.websarva.wings.android.todoapps_kotlin.viewModel

import android.content.Context
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageDownloadRepositoryClient
import com.websarva.wings.android.todoapps_kotlin.repository.FirebaseStorageUploadRepositoryClient
import kotlinx.coroutines.*

class TodoViewModel(
    private val firebaseStorageUploadRepository: FirebaseStorageUploadRepositoryClient,
    private val firebaseStorageDownloadRepository: FirebaseStorageDownloadRepositoryClient
): ViewModel() {
    fun upload(context: Context, storage: FirebaseStorage, auth: FirebaseAuth){
        firebaseStorageUploadRepository.upload(context, storage, auth)
    }

    fun download(context: Context, storage: FirebaseStorage, auth: FirebaseAuth){
        firebaseStorageDownloadRepository.download(context, storage, auth)
    }
}