package com.websarva.wings.android.todoapps_kotlin.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.websarva.wings.android.todoapps_kotlin.R

class ParentPreferenceFragment: PreferenceFragmentCompat() {
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
}