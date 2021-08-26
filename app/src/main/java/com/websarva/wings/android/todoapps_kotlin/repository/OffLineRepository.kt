package com.websarva.wings.android.todoapps_kotlin.repository

import android.app.Activity
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

interface OffLineRepository {
    fun write(activity: Activity)
    fun read(activity: Activity): String?
    fun delete()
}

class OffLineRepositoryClient: OffLineRepository {
    override fun write(activity: Activity) {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        val buf = StringBuffer()

        with(createPreference(activity).edit()){
            for (i in bytes.indices){
                buf.append(String.format("%02x", bytes[i]))
            }
            putString("pass", buf.toString())
            apply()
        }
    }

    override fun read(activity: Activity): String? {
        return createPreference(activity).getString("pass", "")
    }

    override fun delete() {
        TODO("Not yet implemented")
    }

    private fun createPreference(activity: Activity): SharedPreferences {
        val mainKey = MasterKey.Builder(activity)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            activity,
            "offline",
            mainKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }
}