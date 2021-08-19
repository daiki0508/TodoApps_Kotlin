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
import com.websarva.wings.android.todoapps_kotlin.viewModel.TodoViewModel
import java.io.File
import java.io.IOException

interface FirebaseStorageRepository {
    fun upload(context: Context ,storage: FirebaseStorage, auth: FirebaseAuth, task: String?, flag: Boolean)
    fun download(
        context: Context,
        addViewModel: AddTodoTaskViewModel?,
        todoViewModel: TodoViewModel?,
        storage: FirebaseStorage,
        auth: FirebaseAuth,
        tasks: String?,
        flag: Boolean
    ): Boolean
    fun delete(storage: FirebaseStorage, auth: FirebaseAuth, task: String?, flag: Boolean)
}

class FirebaseStorageRepositoryClient: FirebaseStorageRepository {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var position: Int = 0
    private var tasks = ""

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
        addViewModel: AddTodoTaskViewModel?,
        todoViewModel: TodoViewModel?,
        storage: FirebaseStorage,
        auth: FirebaseAuth,
        tasks: String?,
        flag: Boolean
    ): Boolean {
        val uid = auth.currentUser!!.uid

        val storageRef = storage.reference

        if (!flag){
            this.tasks = tasks!!
            val cnt = this.tasks.split(" ").size
            if (cnt == 1){
                downloadTask(context, addViewModel, todoViewModel, "task/$tasks/task", storageRef, uid, flag, cnt)
                downloadTask(context, addViewModel, todoViewModel, "task/$tasks/iv_aes", storageRef, uid, flag, cnt)
                downloadTask(context, addViewModel, todoViewModel, "task/$tasks/salt", storageRef, uid, flag, cnt)
            }else{
                this.auth = auth
                this.storage = storage
                val task = this.tasks.split(" ")[position]
                downloadTask(context, addViewModel, todoViewModel, "task/$task/task", storageRef, uid, flag, cnt)
            }
        }else{
            downloadTask(context, addViewModel, todoViewModel, "list", storageRef, uid, flag, cnt = null)
            downloadTask(context, addViewModel, todoViewModel, "iv_aes", storageRef, uid, flag, cnt = null)
            downloadTask(context, addViewModel, todoViewModel, "salt", storageRef, uid, flag, cnt = null)
        }

        return true
    }

    override fun delete(storage: FirebaseStorage, auth: FirebaseAuth, task: String?, flag: Boolean) {
        val uid = auth.currentUser!!.uid

        val storageRef = storage.reference

        if (flag){
            deleteTask("list", storageRef, uid, flag)
            deleteTask("iv_aes", storageRef, uid, flag)
            deleteTask("salt", storageRef, uid, flag)
        }else{
            deleteTask("task/$task/task", storageRef, uid, flag)
            deleteTask("task/$task/iv_aes", storageRef, uid, flag)
            deleteTask("task/$task/salt", storageRef, uid, flag)
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
        addViewModel: AddTodoTaskViewModel?,
        todoViewModel: TodoViewModel?,
        child: String,
        storageRef: StorageReference,
        uid: String,
        flag: Boolean,
        cnt: Int?
    ){
        val file = File(context.filesDir, child)
        //val file = File.createTempFile(child, null)
        if (!flag){
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
        }else{
            file.createNewFile()
        }

        val fileRef = if (!flag){
            storageRef.child("users/$uid/todo/$child")
        }else{
            storageRef.child("users/$uid/todo/list/$child")
        }
        fileRef.getFile(file).addOnSuccessListener {
            Log.d("test2", "Success!!")
            val completeFlag = if ((!flag && todoViewModel != null) or (flag && todoViewModel != null)){
                todoViewModel!!.completeFlag().value!!
            }else{
                addViewModel!!.completeFlag().value!!
            }
            if (!flag && addViewModel != null){
                completeFlag["${Uri.fromFile(file).lastPathSegment!!}_task"] = true
                addViewModel.setCompleteFlag(completeFlag)
            }else if (!flag){
                position++
                if (cnt == position){
                    completeFlag["${Uri.fromFile(file).lastPathSegment!!}_task"] = true
                    todoViewModel!!.setCompleteFlag(completeFlag)
                }else{
                    download(context, addViewModel = null, todoViewModel, storage, auth, tasks, flag)
                }
            } else{
                completeFlag["${Uri.fromFile(file).lastPathSegment!!}_list"] = true
                todoViewModel!!.setCompleteFlag(completeFlag)
            }
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