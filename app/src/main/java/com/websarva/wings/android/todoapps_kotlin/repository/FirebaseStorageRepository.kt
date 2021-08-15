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
    fun upload(context: Context ,storage: FirebaseStorage, auth: FirebaseAuth, task: String?, flag: Boolean)
}

interface FirebaseStorageDownloadRepository{
    fun download(context: Context, storage: FirebaseStorage, auth: FirebaseAuth, task: String?, flag: Boolean)
}

class FirebaseStorageUploadRepositoryClient: FirebaseStorageUploadRepository {
    override fun upload(
        context: Context,
        storage: FirebaseStorage,
        auth: FirebaseAuth,
        task: String?,
        flag: Boolean
    ) {
        val uid = auth.currentUser!!.uid

        val storageRef = storage.reference

        if (flag){
            uploadTask(context, "list", storageRef, uid, null, flag)
            uploadTask(context, "iv_aes", storageRef, uid, null, flag)
            uploadTask(context, "salt", storageRef, uid, null, flag)
        }else{
            uploadTask(context, "task/$task/task", storageRef, uid, task, flag)
            uploadTask(context, "task/$task/iv_aes", storageRef, uid, task, flag)
            uploadTask(context, "task/$task/salt", storageRef, uid, task, flag)
        }
    }

    private fun uploadTask(
        context: Context,
        child: String,
        storageRef: StorageReference,
        uid: String,
        task: String?,
        flag: Boolean
    ){
        val file = Uri.fromFile(File(context.filesDir, child))

        val fileRef: StorageReference = if (flag){
            storageRef.child("users/$uid/todo/list/${file.lastPathSegment}")
        }else{
            storageRef.child("users/$uid/todo/task/$task/${file.lastPathSegment}")
        }

        val uploadTask = fileRef.putFile(file)
        uploadTask.addOnFailureListener {
            Log.w("test", it)
        }.addOnSuccessListener {
            Log.d("test", "success!!")
        }
    }
}

class FirebaseStorageDownloadRepositoryClient: FirebaseStorageDownloadRepository {
    override fun download(
        context: Context,
        storage: FirebaseStorage,
        auth: FirebaseAuth,
        task: String?,
        flag: Boolean
    ) {
        val uid = auth.currentUser!!.uid

        val storageRef = storage.reference

        if (flag){
            downloadTask(context, "list", storageRef, uid, flag)
            downloadTask(context, "iv_aes", storageRef, uid, flag)
            downloadTask(context, "salt", storageRef, uid, flag)
        }else{
            downloadTask(context, "task/$task/task", storageRef, uid, flag)
            downloadTask(context, "task/$task/iv_aes", storageRef, uid, flag)
            downloadTask(context, "task/$task/salt", storageRef, uid, flag)
        }
    }

    private fun downloadTask(
        context: Context,
        child: String,
        storageRef: StorageReference,
        uid: String,
        flag: Boolean
    ){
        val file = File(context.filesDir, child)
        //val file = File.createTempFile(child, null)
        val fileRef = if (flag){
            storageRef.child("users/$uid/todo/list/$child")
        }else{
            storageRef.child("users/$uid/todo/$child")
        }
        fileRef.getFile(file).addOnSuccessListener {
            Log.d("test2", "Success!!")
        }.addOnFailureListener {
            Log.w("test2", it)
        }
    }
}