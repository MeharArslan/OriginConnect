package com.meharenterprises.originconnect.ui.auth
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.model.Country
import com.meharenterprises.originconnect.data.model.CountryList
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhoneActivity : AppCompatActivity() {
    private val vm: AuthViewModel by viewModels()
    private var selectedCountry = CountryList.all.find { it.code == "PK" } ?: CountryList.all.first()

    private val countryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val name = result.data?.getStringExtra("country_name") ?: return@registerForActivityResult
            selectedCountry = CountryList.all.find { it.name == name } ?: return@registerForActivityResult
            updateCountryUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnCountry = findViewById<LinearLayout>(R.id.btnCountryCode)
        val tvFlag = findViewById<TextView>(R.id.tvFlag)
        val tvCode = findViewById<TextView>(R.id.tvCode)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val tvError = findViewById<TextView>(R.id.tvPhoneError)
        val btnContinue = findViewById<Button>(R.id.btnContinue)

        btnBack.setOnClickListener { finish() }

        btnCountry.setOnClickListener {
            countryLauncher.launch(Intent(this, CountryActivity::class.java))
        }

        etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().trim()
                val valid = phone.length >= 7
                btnContinue.isEnabled = valid
                btnContinue.background = if (valid)
                    getDrawable(R.drawable.bg_btn_primary)
                else getDrawable(R.drawable.bg_btn_disabled)
                btnContinue.setTextColor(if (valid)
                    getColor(R.color.oc_text_primary)
                else getColor(R.color.oc_text_secondary))
                tvError.visibility = View.GONE
            }
        })

        btnContinue.setOnClickListener {
            val phone = "${selectedCountry.dialCode}${etPhone.text.toString().trim()}"
            vm.sendOtp(phone)
        }

        vm.state.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> btnContinue.isEnabled = false
                is AuthState.OtpSent -> {
                    val phone = "${selectedCountry.dialCode}${etPhone.text.toString().trim()}"
                    startActivity(Intent(this, OtpActivity::class.java).apply {
                        putExtra("PHONE", phone)
                        putExtra("CODE", state.code)
                    })
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                is AuthState.Error -> {
                    tvError.text = state.message
                    tvError.visibility = View.VISIBLE
                    btnContinue.isEnabled = true
                }
                else -> {}
            }
        }

        updateCountryUI()
    }

    private fun updateCountryUI() {
        findViewById<TextView>(R.id.tvFlag).text = selectedCountry.flag
        findViewById<TextView>(R.id.tvCode).text = selectedCountry.dialCode
    }
}
