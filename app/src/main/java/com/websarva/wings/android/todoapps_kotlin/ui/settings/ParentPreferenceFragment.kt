package com.websarva.wings.android.todoapps_kotlin.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.websarva.wings.android.todoapps_kotlin.R

class ParentPreferenceFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
    }
}