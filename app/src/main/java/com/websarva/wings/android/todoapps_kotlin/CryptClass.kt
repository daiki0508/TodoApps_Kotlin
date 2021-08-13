package com.websarva.wings.android.todoapps_kotlin

import android.content.Context
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.security.auth.Destroyable
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec


class CryptClass {
    fun encrypt(context: Context, pass: CharArray, pStr: String){
        val key = generateStrongAESKey(context, pass, 256, true)
        val list = File(context.filesDir, "list")

        val iv = File(context.filesDir, "iv")

        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)

            list.writeBytes(cipher.doFinal(pStr.toByteArray()))

            iv.writeBytes(cipher.iv)
        }finally {
            key.destroy()
        }
    }

    fun decrypt(context: Context, pass: CharArray, pStr: String){
        //TODO("Firebaseとの通信が未実装のため")
        val listFile = File(context.filesDir, "list")
        if (!listFile.exists()){
            Log.d("test2", "Called!")
            encrypt(context, pass, pStr)
            return
        }
        val key = generateStrongAESKey(context, pass, 256, false)
        val ivFile = context.openFileInput("iv")

        try {
            val iv = ByteArray(ivFile.available())
            ivFile.read(iv)
            val ips = IvParameterSpec(iv)

            val list: ByteArray = listFile.readBytes()

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key, ips)

            Log.d("test", "${String(cipher.doFinal(list))}\n$pStr")
            encrypt(context, pass, "${String(cipher.doFinal(list))}\n$pStr")
        }finally {
            key.destroy()
            ivFile.close()
        }
    }

    private fun generateStrongAESKey(context: Context, password: CharArray, keyLength: Int, flag: Boolean): SecretKey{
        val internalCount = 10000
        val saltLength = keyLength / 8

        val salt = ByteArray(saltLength)
        if (flag){
            //random.nextBytes(salt)
            for (i in salt.indices) {
                salt[i] = password[i].code.toByte()
            }

            val saltFile = File(context.filesDir, "salt")
            saltFile.writeBytes(salt)
        }else{
            val saltFile = context.openFileInput("salt")
            saltFile.read(salt)
        }
        val keySpec = PBEKeySpec(password, salt, internalCount, keyLength)
        val keyFactory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            SecretKeyFactory.getInstance("PBKDF2withHmacSHA256")
        }else{
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        }
        val keyBytes = keyFactory.generateSecret(keySpec).encoded

        return SecureSecretKey(keyBytes, "AES")
    }
}

private class SecureSecretKey(key2: ByteArray, algorithm: String): SecretKey, Destroyable {
    private var key: ByteArray?
    private val algorithm: String

    override fun getAlgorithm(): String {
        return algorithm
    }

    override fun getFormat(): String {
        return "RAW"
    }

    override fun getEncoded(): ByteArray {
        if (key == null){
            throw NullPointerException()
        }
        return key!!.clone()
    }

    override fun destroy() {
        if (isDestroyed){
            return
        }
        val dummy = "E/AndroidRuntime: FATAL EXCEPTION: main Process: " +
                "com.websarva.wings.android.androidkeystoresample_kotlin, PID: 6147 " +
                "java.lang.RuntimeException"
        val nonSecret: ByteArray = dummy.toByteArray(Charsets.ISO_8859_1)

        for (i in key!!.indices){
            key!![i] = nonSecret[i % nonSecret.size]
        }
        val out= FileOutputStream("/dev/null")
        out.write(key)
        out.flush()
        out.close()
        key = null
        System.gc()
    }

    override fun isDestroyed(): Boolean {
        return key == null
    }

    init {
        this.key = key2.clone()
        this.algorithm = algorithm
        Arrays.fill(key2, 0)
    }
}