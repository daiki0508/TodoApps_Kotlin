package com.websarva.wings.android.todoapps_kotlin.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.websarva.wings.android.todoapps_kotlin.R
import com.websarva.wings.android.todoapps_kotlin.ui.DialogListener
import com.websarva.wings.android.todoapps_kotlin.ui.main.MainActivity

interface OnClickListener{
    fun onClickListener()
}

class ParentPreferenceFragment: PreferenceFragmentCompat() {
    private lateinit var listener: OnClickListener

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        findPreference<ListPreference>("theme")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                when(newValue.toString()){
                    "0" -> {
                        Log.d("theme", newValue.toString())
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    "1" ->{
                        Log.d("theme", newValue.toString())
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
                true
            }
        }
        findPreference<Preference>("backup")?.apply {
            setOnPreferenceClickListener {
                listener.onClickListener()

                true
            }
        }
        findPreference<Preference>("signout")?.apply {
            setOnPreferenceClickListener {
                // googleとFirebaseから完全にサインアウトする
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("190718735404-vhsbe83o61jl3i2834rka0jh1hho5ibc.apps.googleusercontent.com")
                    .requestEmail()
                    .build()
                Firebase.auth.signOut()
                GoogleSignIn.getClient(requireActivity(), gso).signOut()

                // ログイン画面に遷移
                activity?.let {
                    Intent(it, MainActivity::class.java).apply {
                        startActivity(this)
                        it.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        it.finish()
                    }
                }
                true
            }
        }

        findPreference<Preference>("evaluation")?.apply {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=com.websarva.wings.android.todoapps")
            this.intent = intent
        }
        findPreference<Preference>("form")?.apply {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://forms.gle/q52d6a6a19aJ3eB78")
            this.intent = intent
        }
        findPreference<Preference>("license")?.apply {
            //TODO("ライセンス未発行")
        }
        findPreference<Preference>("policy")?.apply {
            //TODO("ポリシー未作成")
        }
    }

    fun setOnClickListener(listener: OnClickListener){
        this.listener = listener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as OnClickListener
        }catch (e: Exception){
            Log.wtf("ERROR", "CANNOT FIND LISTENER")
        }
    }
}