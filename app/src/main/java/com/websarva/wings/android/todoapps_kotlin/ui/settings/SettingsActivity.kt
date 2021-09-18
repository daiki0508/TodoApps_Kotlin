package com.websarva.wings.android.todoapps_kotlin.ui.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivitySettingsBinding
import com.websarva.wings.android.todoapps_kotlin.model.DownloadStatus
import com.websarva.wings.android.todoapps_kotlin.model.FileName
import com.websarva.wings.android.todoapps_kotlin.ui.DialogListener
import com.websarva.wings.android.todoapps_kotlin.ui.add.AddTodoTaskActivity
import com.websarva.wings.android.todoapps_kotlin.ui.fragment.todo.TodoFragment
import com.websarva.wings.android.todoapps_kotlin.viewModel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity(), OnClickListener, DialogListener {
    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModel()

    private lateinit var list: String
    private var position: Int? = null
    private var flag: Boolean? = null
    private var networkStatus: Boolean? = null

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    companion object{
        const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

        viewModel.setInit(this)

        // falseがAddTodoTaskActivity
        flag = intent.getBooleanExtra("flag", true)
        networkStatus = intent.getBooleanExtra("network", false)
        if (!flag!!){
            list = intent.getStringExtra(FileName().list)!!
            position = intent.getIntExtra("position", 0)
        }

        // observer通知
        viewModel.completeFlag().observe(this, {
            if ((it[DownloadStatus().list] == true) and (it[DownloadStatus().iv_aes_list] == true) and (it[DownloadStatus().salt_list] == true)){
                viewModel.restore(auth, storage, flag = false)
                Toast.makeText(this, "復元が完了しました！", Toast.LENGTH_LONG).show()
                this.networkStatus = true
            }
        })
    }

    override fun onBackPressed() {
        // trueがTodoActivity
        if (flag!!){
            Intent(this, TodoFragment::class.java).apply {
                this.putExtra("network", networkStatus)
                startActivity(this)
                finish()
            }
        }else{
            Intent(this, AddTodoTaskActivity::class.java).apply {
                this.putExtra("network", networkStatus)
                this.putExtra(FileName().list, list)
                this.putExtra("position", position)
                startActivity(this)
                finish()
            }
        }
    }

    override fun onClickListener() {
        if (networkStatus == true){
            Toast.makeText(this, "既に最新の状態です。", Toast.LENGTH_LONG).show()
        }else{
            if (viewModel.connectingStatus() != null){
                auth = FirebaseAuth.getInstance()
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("190718735404-vhsbe83o61jl3i2834rka0jh1hho5ibc.apps.googleusercontent.com")
                    .requestEmail()
                    .build()

                googleSignInClient = GoogleSignIn.getClient(this, gso)

                startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
            }else{
                Toast.makeText(this, "ネットワークに接続してください！！", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                //Log.d("setting", account.idToken!!)
                firebaseAuthWithGoogle(account.idToken!!)
            }catch (e: ApiException){
                Log.w("setting", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        viewModel.firebaseAuthWithGoogle(auth, idToken)
            .addOnCompleteListener(this){task ->
                if (task.isSuccessful){
                    SelectModeDialog().show(supportFragmentManager, "SelectModeDialogFragment")
                }else{
                    Toast.makeText(this, "認証エラー", Toast.LENGTH_SHORT).show()
                }
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
        // trueがバックアップ
        storage = FirebaseStorage.getInstance()
        if (flag){
            viewModel.backup(auth, storage)
        }else{
            // trueがlist処理
            viewModel.restore(auth, storage, flag = true)
        }
    }
}