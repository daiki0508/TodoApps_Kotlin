package com.websarva.wings.android.todoapps_kotlin.repository

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.websarva.wings.android.todoapps_kotlin.ui.main.MainActivity

interface FirebaseTopRepository {
    fun firebaseAuthWithGoogle(auth: FirebaseAuth, idToken: String): Task<AuthResult>
}

class FirebaseTopRepositoryClient: FirebaseTopRepository {
    override fun firebaseAuthWithGoogle(
        auth: FirebaseAuth,
        idToken: String
    ): Task<AuthResult>{
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        return auth.signInWithCredential(credential)
    }
}