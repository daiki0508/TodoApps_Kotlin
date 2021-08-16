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
import java.io.FileInputStream
import java.io.FileWriter
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec


class CryptClass {
    private fun encrypt(context: Context, pass: CharArray, pStr: String, type: Int, task: String?){
        val key = generateStrongAESKey(context, pass, 256, true, type, task)
        val list: File = if (type == 0){
            File(context.filesDir, "list")
        }else{
            File("${context.filesDir}/task/$task", "task")
        }

        val iv: File = if (type == 0){
            File(context.filesDir, "iv_aes")
        }else{
            File("${context.filesDir}/task/$task", "iv_aes")
        }

        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)

            list.writeBytes(cipher.doFinal(pStr.toByteArray()))

            iv.writeBytes(cipher.iv)
        }finally {
            key.destroy()
        }
    }

    fun decrypt(context: Context, pass: CharArray, pStr: String, type: Int, task: String?, flag: Boolean): String?{
        val encFile: File = if (type == 0){
            File(context.filesDir, "list")
        }else{
            File("${context.filesDir}/task/$task", "task")
        }
        if (!encFile.exists()){
            // typeが1の場合かつ、対象ファイルが存在しない場合はサブディレクトリを作成
            if (type == 1 && !File("${context.filesDir}/task/$task").exists()){
                File("${context.filesDir}/task/$task").mkdirs()
            }else if (type == 2){
                // 新規にListは作られたが、まだTaskは作成されていない状態
                return "NoTask"
            }
            Log.d("test2", "Called!")
            encrypt(context, pass, pStr, type, task)
            return null
        }
        val key = generateStrongAESKey(context, pass, 256, false, type, task)
        val ivFile: FileInputStream = if (type == 0){
            context.openFileInput("iv_aes")
        }else{
            // ネストされたディレクトリの場合はFileInputStreamでないとエラーが発生
            FileInputStream("${context.filesDir}/task/$task/iv_aes")
        }

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val enc: ByteArray

        try {
            val iv = ByteArray(ivFile.available())
            ivFile.read(iv)
            val ips = IvParameterSpec(iv)

            enc = encFile.readBytes()

            cipher.init(Cipher.DECRYPT_MODE, key, ips)

            Log.d("test", "${String(cipher.doFinal(enc))} $pStr")
            if (flag){
                // typeが3の場合は更新
                if (type == 3){
                    encrypt(context, pass, pStr, type, task)
                }else{
                    encrypt(context, pass, "${String(cipher.doFinal(enc))} $pStr", type, task)
                }
            }
        }finally {
            key.destroy()
            ivFile.close()
        }
        if (!flag){
            return String(cipher.doFinal(enc))
        }
        return null
    }

    private fun generateStrongAESKey(context: Context, password: CharArray, keyLength: Int, flag: Boolean, type: Int, task: String?): SecretKey{
        val internalCount = 10000
        val saltLength = keyLength / 8

        val salt = ByteArray(saltLength)
        if (flag){
            //random.nextBytes(salt)
            for (i in salt.indices) {
                salt[i] = password[i].code.toByte()
            }

            val saltFile: File = if (type == 0){
                File(context.filesDir, "salt")
            }else{
                File("${context.filesDir}/task/$task", "salt")
            }
            saltFile.writeBytes(salt)
        }else{
            val saltFile: FileInputStream = if (type == 0){
                context.openFileInput("salt")
            }else{
                // ネストされたディレクトリの場合はFileInputStreamでないとエラーが発生
                FileInputStream("${context.filesDir}/task/$task/salt")
            }
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