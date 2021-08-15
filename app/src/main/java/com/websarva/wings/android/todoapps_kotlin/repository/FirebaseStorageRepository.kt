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
    fun download(context: Context, storage: FirebaseStorage, auth: FirebaseAuth)
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
        uploadTask(context, "iv_aes", storageRef, uid)
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

class FirebaseStorageDownloadRepositoryClient: FirebaseStorageDownloadRepository {
    override fun download(context: Context, storage: FirebaseStorage, auth: FirebaseAuth) {
        val uid = auth.currentUser!!.uid

        val storageRef = storage.reference

        downloadTask(context, "list", storageRef, uid)
        downloadTask(context, "iv_aes", storageRef, uid)
        downloadTask(context, "salt", storageRef, uid)
    }

    private fun downloadTask(context: Context, child: String, storageRef: StorageReference, uid: String){
        val file = File(context.filesDir, child)
        //val file = File.createTempFile(child, null)
        val fileRef = storageRef.child("users/$uid/todo/list/$child")
        fileRef.getFile(file).addOnSuccessListener {
            Log.d("test2", "Success!!")
        }.addOnFailureListener {
            Log.w("test2", it)
        }
    }
}