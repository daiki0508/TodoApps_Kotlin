package com.websarva.wings.android.todoapps_kotlin.ui.main

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.play.core.install.model.ActivityResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.websarva.wings.android.todoapps_kotlin.BuildConfig
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityMainBinding
import com.websarva.wings.android.todoapps_kotlin.ui.DialogListener
import com.websarva.wings.android.todoapps_kotlin.ui.NetWorkFailureDialog
import com.websarva.wings.android.todoapps_kotlin.ui.todo.TodoActivity
import com.websarva.wings.android.todoapps_kotlin.viewModel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), DialogListener {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object{
        const val RC_SIGN_IN = 9001
        const val RC_IMMEDIATE_UPDATE = 100
    }

    override fun onStart() {
        super.onStart()

        // アプリのアップデート開始
        if (viewModel.connectingStatus(this) != null && !BuildConfig.DEBUG){
            viewModel.appUpdate(this)
        }
    }

    override fun onResume() {
        super.onResume()

        // アプリのアップデートの再開
        if (viewModel.connectingStatus(this) != null && !BuildConfig.DEBUG){
            viewModel.restartUpdate(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

        binding.googleLoginButton.setSize(SignInButton.SIZE_WIDE)

        binding.googleLoginButton.setOnClickListener {
            // ネットワーク状況によって処理を分岐
            if (viewModel.connectingStatus(this) != null){
                auth = Firebase.auth
                googleSign()
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

        // RC_SIGN_INはGoogle認証
        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                //Log.d("test", account.idToken!!)
                firebaseAuthWithGoogle(account.idToken!!)
            }catch (e: ApiException){
                Log.w("GoogleSignIn", "Google sign in failed", e)
            }
        // RC_IMMEDIATE_UPDATEはアプリのアップデート
        }else if (requestCode == RC_IMMEDIATE_UPDATE){
            when(resultCode){
                // アップデート同意前に×ボタンまたはバックキー押下で案内画面が閉じられた時
                Activity.RESULT_CANCELED -> finish()
                // 何らかのエラーによってアップデートが行えない時
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED ->{
                }
                // アップデート同意後、バックキー押下で更新画面が閉じられた時
                Activity.RESULT_OK -> finish()
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

    fun todoIntent(flag: Boolean){
        // trueがNetWork接続状態
        Intent(this, TodoActivity::class.java).apply {
            this.putExtra("network", flag)
            startActivity(this)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    override fun onDialogFlagReceive(
        dialog: DialogFragment,
        list: String,
        type: Int,
        flag: Boolean,
        position: Int?
    ) {
        return
    }

    override fun onDialogReceive(flag: Boolean) {
        if (flag){
            todoIntent(flag = false)
        }
    }
}