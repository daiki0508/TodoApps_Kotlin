package com.websarva.wings.android.todoapps_kotlin.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File

interface FirebaseStorageUploadRepository {
    fun upload(context: Context ,storage: FirebaseStorage, auth: FirebaseAuth)
}

interface FirebaseStorageDownloadRepository{
    fun download(){
        TODO("未実装")
    }
}

class FirebaseStorageUploadRepositoryClient: FirebaseStorageUploadRepository {
    override fun upload(
        context: Context,
        storage: FirebaseStorage,
        auth: FirebaseAuth
    ) {
        val uid = auth.currentUser!!.uid

        val storageRef = storage.reference

        uploadTask(context, "list", storageRef, uid)
        uploadTask(context, "iv", storageRef, uid)
        uploadTask(context, "salt", storageRef, uid)
    }

    private fun uploadTask(context: Context, child: String, storageRef: StorageReference, uid: String){
        val file = Uri.fromFile(File(context.filesDir, child))

        val fileRef = storageRef.child("users/$uid/todo/list/${file.lastPathSegment}")

        val uploadTask = fileRef.putFile(file)
        uploadTask.addOnFailureListener {
            Log.w("test", it)
        }.addOnSuccessListener {
            Log.d("test", "success!!")
        }
    }
}