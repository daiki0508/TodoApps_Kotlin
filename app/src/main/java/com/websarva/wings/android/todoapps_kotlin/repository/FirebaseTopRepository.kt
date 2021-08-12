package com.websarva.wings.android.todoapps_kotlin.repository

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider

interface FirebaseTopRepository {
    fun firebaseAuthWithGoogle(activity: Activity ,auth: FirebaseAuth, idToken: String): Boolean
}

class FirebaseTopRepositoryClient: FirebaseTopRepository {
    override fun firebaseAuthWithGoogle(
        activity: Activity,
        auth: FirebaseAuth,
        idToken: String
    ): Boolean {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        var flag = false

        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity){task ->
                if (task.isSuccessful){
                    flag = true
                }
            }
        return flag
    }
}