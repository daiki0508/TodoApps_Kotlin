package com.websarva.wings.android.todoapps_kotlin.ui.licenses

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.websarva.wings.android.todoapps_kotlin.databinding.ActivityLicensesBinding

class LicensesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLicensesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLicensesBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

        binding.webview.let {
            it.settings.allowFileAccess = false
            it.settings.allowContentAccess = false
            it.settings.javaScriptEnabled = false

            it.loadUrl("file:///android_asset/licenses.html")
        }
    }
}