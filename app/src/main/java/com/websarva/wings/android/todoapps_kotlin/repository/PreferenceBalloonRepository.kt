package com.websarva.wings.android.todoapps_kotlin.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.websarva.wings.android.todoapps_kotlin.model.Balloon
import com.websarva.wings.android.todoapps_kotlin.model.PreferenceBalloon

interface PreferenceBalloonRepository {
    suspend fun save(context: Context, flag: Boolean)
    suspend fun read(context: Context, flag: Boolean): PreferenceBalloon
}

class PreferenceBalloonRepositoryClient: PreferenceBalloonRepository {
    override suspend fun save(context: Context, flag: Boolean) {
        with(createPreference(context, flag).edit()){
            putBoolean(Balloon.Balloon.name, false)
            apply()
        }
    }

    override suspend fun read(context: Context, flag: Boolean): PreferenceBalloon {
        return PreferenceBalloon(retBalloon = createPreference(context, flag).getBoolean(Balloon.Balloon.name, true))
    }

    private fun createPreference(context: Context, flag: Boolean): SharedPreferences {
        val mainKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            // trueがtodo, falseがadd
            if (flag){
                "balloon_todo"
            }else{
                "balloon_add"
            },
            mainKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }
}