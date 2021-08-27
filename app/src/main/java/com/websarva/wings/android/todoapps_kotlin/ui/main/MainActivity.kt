package com.websarva.wings.android.todoapps_kotlin.ui.main

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityMainBinding
import com.websarva.wings.android.todoapps_kotlin.ui.todo.TodoActivity
import com.websarva.wings.android.todoapps_kotlin.viewModel.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), DialogListener{
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object{
        const val RC_SIGN_IN = 9001
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null){
            Log.d("test", "CurrentUser")

            // ネットワーク状況によって処理を分岐
            if (viewModel.connectingStatus(this) != null){
                todoIntent(flag = true)
            }else{
                todoIntent(flag = false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

        auth = Firebase.auth

        binding.googleLoginButton.setSize(SignInButton.SIZE_WIDE)

        googleSign()

        binding.googleLoginButton.setOnClickListener {
            // ネットワーク状況によって処理を分岐
            if (viewModel.connectingStatus(this) != null){
                startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
            }else{
                NetWorkFailureDialog(flag = true).show(supportFragmentManager, "NetWorkFailureDialog")
            }
        }
    }

    private fun googleSign(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("190718735404-vhsbe83o61jl3i2834rka0jh1hho5ibc.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("test", account.idToken!!)
                firebaseAuthWithGoogle(account.idToken!!)
            }catch (e: ApiException){
                Log.w("test", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        viewModel.firebaseAuthWithGoogle(this, auth, idToken)
            .addOnCompleteListener(this){task ->
                if (task.isSuccessful){
                    todoIntent(flag = true)
                }else{
                    Toast.makeText(this, "認証エラー", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun todoIntent(flag: Boolean){
        // trueがNetWork接続状態
        Intent(this, TodoActivity::class.java).apply {
            this.putExtra("network", flag)
            startActivity(this)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    override fun onDialogReceive(flag: Boolean) {
        if (flag){
            todoIntent(flag = false)
        }
    }
}