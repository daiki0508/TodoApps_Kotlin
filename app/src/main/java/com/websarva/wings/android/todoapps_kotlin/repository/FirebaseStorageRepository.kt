package com.websarva.wings.android.todoapps_kotlin.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.websarva.wings.android.todoapps_kotlin.viewModel.AddTodoTaskViewModel
import java.io.File
import java.io.IOException

interface FirebaseStorageRepository {
    fun upload(context: Context ,storage: FirebaseStorage, auth: FirebaseAuth, task: String?, flag: Boolean)
    fun download(context: Context,viewModel: AddTodoTaskViewModel, storage: FirebaseStorage, auth: FirebaseAuth, task: String?, flag: Boolean): Boolean
    fun delete(storage: FirebaseStorage, auth: FirebaseAuth, task: String?)
}

class FirebaseStorageRepositoryClient: FirebaseStorageRepository {
    override fun upload(
        context: Context,
        storage: FirebaseStorage,
        auth: FirebaseAuth,
        task: String?,
        flag: Boolean
    ) {
        val uid = auth.currentUser!!.uid

        val storageRef = storage.reference

        if (!flag){
            uploadTask(context, "list", storageRef, uid, null, flag)
            uploadTask(context, "iv_aes", storageRef, uid, null, flag)
            uploadTask(context, "salt", storageRef, uid, null, flag)
        }else{
            uploadTask(context, "task/$task/task", storageRef, uid, task, flag)
            uploadTask(context, "task/$task/iv_aes", storageRef, uid, task, flag)
            uploadTask(context, "task/$task/salt", storageRef, uid, task, flag)
        }
    }

    override fun download(
        context: Context,
        viewModel: AddTodoTaskViewModel,
        storage: FirebaseStorage,
        auth: FirebaseAuth,
        task: String?,
        flag: Boolean
    ): Boolean {
        val uid = auth.currentUser!!.uid

        val storageRef = storage.reference

        if (flag){
            downloadTask(context, viewModel, "list", storageRef, uid, flag)
            downloadTask(context, viewModel, "iv_aes", storageRef, uid, flag)
            downloadTask(context, viewModel, "salt", storageRef, uid, flag)
        }else{
            downloadTask(context, viewModel, "task/$task/task", storageRef, uid, flag)
            downloadTask(context, viewModel, "task/$task/iv_aes", storageRef, uid, flag)
            downloadTask(context, viewModel, "task/$task/salt", storageRef, uid, flag)
        }

        return true
    }

    override fun delete(storage: FirebaseStorage, auth: FirebaseAuth, task: String?) {
        val uid = auth.currentUser!!.uid

        val storageRef = storage.reference

        deleteTask("task/$task/task", storageRef, uid, flag = false)
        deleteTask("task/$task/iv_aes", storageRef, uid, flag = false)
        deleteTask("task/$task/salt", storageRef, uid, flag = false)
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

        val fileRef: StorageReference = if (!flag){
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

    private fun downloadTask(
        context: Context,
        viewModel: AddTodoTaskViewModel,
        child: String,
        storageRef: StorageReference,
        uid: String,
        flag: Boolean
    ){
        val file = File(context.filesDir, child)
        //val file = File.createTempFile(child, null)
        if (!file.exists()){
            try {
                file.createNewFile()
            }catch (e: IOException){
                File(File(context.filesDir, child).parent!!).mkdirs()
                try {
                    file.createNewFile()
                }catch (e: IOException){
                    Log.e("ERROR", e.toString())
                }
            }
        }

        val fileRef = if (flag){
            storageRef.child("users/$uid/todo/list/$child")
        }else{
            storageRef.child("users/$uid/todo/$child")
        }
        fileRef.getFile(file).addOnSuccessListener {
            Log.d("test2", "Success!!")
            val completeFlag = viewModel.completeFlag().value!!
            completeFlag[Uri.fromFile(file).lastPathSegment!!] = true
            viewModel.setCompleteFlag(completeFlag)
        }.addOnFailureListener {
            Log.w("test2", it)
        }
    }

    private fun deleteTask(
        child: String,
        storageRef: StorageReference,
        uid: String,
        flag: Boolean
    ){
        val fileRef = if (flag){
            storageRef.child("users/$uid/todo/list/$child")
        }else{
            storageRef.child("users/$uid/todo/$child")
        }
        fileRef.delete().addOnSuccessListener {
            Log.d("test2", "Success!!")
        }.addOnFailureListener {
            Log.w("test2", it)
        }
    }
}