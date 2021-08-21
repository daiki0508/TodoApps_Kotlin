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
    fun write(activity: Activity, task: String, keyName: String, checkFlag: Boolean)
    fun read(activity: Activity, task: String, keyName: String): Boolean
    fun delete(activity: Activity, list: String)
}

class PreferenceRepositoryClient: PreferenceRepository {
    override fun write(activity: Activity, task: String, keyName: String, checkFlag: Boolean) {
        with(createPreference(activity, task).edit()){
            putBoolean(keyName, checkFlag)
            apply()
        }
    }

    override fun read(activity: Activity, task: String, keyName: String): Boolean {
        //Log.d("preference", "$keyName, ${createPreference(activity, task).getBoolean(keyName, false)}")
        return createPreference(activity, task).getBoolean(keyName, false)
    }

    override fun delete(activity: Activity, list: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            activity.deleteSharedPreferences(list)
        }else{
            File("${activity.filesDir.parent}/shared_prefs/$list").delete()
        }
    }

    private fun createPreference(activity: Activity, task: String): SharedPreferences{
        val mainKey = MasterKey.Builder(activity)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            activity,
            task,
            mainKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }
}