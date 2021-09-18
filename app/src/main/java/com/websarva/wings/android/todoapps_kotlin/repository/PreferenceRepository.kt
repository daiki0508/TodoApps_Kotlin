package com.websarva.wings.android.todoapps_kotlin.repository

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File

interface PreferenceRepository {
    fun write(context: Context, task: String, keyName: String, checkFlag: Boolean)
    fun read(context: Context, task: String, keyName: String): Boolean
    fun delete(context: Context, list: String)
}

class PreferenceRepositoryClient: PreferenceRepository {
    override fun write(context: Context, task: String, keyName: String, checkFlag: Boolean) {
        with(createPreference(context, task).edit()){
            putBoolean(keyName, checkFlag)
            apply()
        }
    }

    override fun read(context: Context, task: String, keyName: String): Boolean {
        return createPreference(context, task).getBoolean(keyName, false)
    }

    override fun delete(context: Context, list: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            context.deleteSharedPreferences(list)
        }else{
            File("${context.filesDir.parent}/shared_prefs/$list.xml").delete()
        }
    }

    private fun createPreference(context: Context, task: String): SharedPreferences{
        val mainKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            task,
            mainKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }
}