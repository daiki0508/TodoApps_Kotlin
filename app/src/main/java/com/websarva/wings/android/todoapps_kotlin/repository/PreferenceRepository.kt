package com.websarva.wings.android.todoapps_kotlin.repository

import android.app.Activity
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

interface PreferenceRepository {
    fun write(activity: Activity, task: String, keyName: String, checkFlag: Boolean)
    fun read(activity: Activity, task: String, keyName: String): Boolean
}

class PreferenceRepositoryClient: PreferenceRepository {
    override fun write(activity: Activity, task: String, keyName: String, checkFlag: Boolean) {
        with(createPreference(activity, task).edit()){
            putBoolean(keyName, checkFlag)
            apply()
        }
    }

    override fun read(activity: Activity, task: String, keyName: String): Boolean {
        return createPreference(activity, task).getBoolean(keyName, false)
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