package com.websarva.wings.android.todoapps_kotlin.repository

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import java.security.SecureRandom

interface OffLineRepository {
    fun write(context: Context)
    fun online(context: Context, auth: FirebaseAuth)
    fun read(context: Context): String?
}

class OffLineRepositoryClient: OffLineRepository {
    override fun write(context: Context) {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        val buf = StringBuffer()

        with(createPreference(context).edit()){
            for (i in bytes.indices){
                buf.append(String.format("%02x", bytes[i]))
            }
            putString("pass", buf.toString())
            apply()
        }
    }

    override fun online(context: Context, auth: FirebaseAuth) {
        with(createPreference(context).edit()){
            putString("pass", "${auth.currentUser!!.uid}0000")
            apply()
        }
    }

    override fun read(context: Context): String? {
        return createPreference(context).getString("pass", "")
    }

    private fun createPreference(context: Context): SharedPreferences {
        val mainKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "offline",
            mainKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }
}