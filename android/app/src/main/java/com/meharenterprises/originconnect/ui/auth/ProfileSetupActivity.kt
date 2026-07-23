package com.meharenterprises.originconnect.ui.auth
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.data.remote.ApiService
import com.meharenterprises.originconnect.data.remote.UpdateProfileRequest
import com.meharenterprises.originconnect.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileSetupActivity : AppCompatActivity() {
    @Inject lateinit var session: SessionManager
    @Inject lateinit var api: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        val etName = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)
        val etAbout = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etAbout)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val progress = findViewById<ProgressBar>(R.id.progress)

        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val valid = s.toString().trim().length >= 2
                btnContinue.isEnabled = valid
                btnContinue.background = if (valid) getDrawable(R.drawable.bg_btn_primary)
                    else getDrawable(R.drawable.bg_btn_disabled)
                btnContinue.setTextColor(if (valid) getColor(R.color.oc_text_primary)
                    else getColor(R.color.oc_text_secondary))
            }
        })

        btnContinue.setOnClickListener {
            val name = etName.text.toString().trim()
            val about = etAbout.text.toString().trim()
            progress.visibility = View.VISIBLE
            btnContinue.isEnabled = false
            lifecycleScope.launch {
                try {
                    api.updateProfile(UpdateProfileRequest(displayName = name, about = about.ifEmpty { null }), session.getAuthHeader())
                } catch (_: Exception) {}
                startActivity(Intent(this@ProfileSetupActivity, MainActivity::class.java))
                finishAffinity()
            }
        }
    }
}
