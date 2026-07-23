package com.meharenterprises.originconnect.ui.auth
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private val vm: AuthViewModel by viewModels()
    private var phone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etOtp = findViewById<EditText>(R.id.etOtp)
        val btnSendOtp = findViewById<Button>(R.id.btnSendOtp)
        val btnVerify = findViewById<Button>(R.id.btnVerify)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val otpLayout = findViewById<View>(R.id.otpLayout)

        btnSendOtp.setOnClickListener {
            phone = etPhone.text.toString().trim()
            if (phone.isEmpty()) { Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            vm.sendOtp(phone)
        }

        btnVerify.setOnClickListener {
            val code = etOtp.text.toString().trim()
            if (code.length != 6) { Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            vm.verifyOtp(phone, code)
        }

        vm.state.observe(this) { state ->
            progress.visibility = if (state is AuthState.Loading) View.VISIBLE else View.GONE
            when (state) {
                is AuthState.OtpSent -> {
                    otpLayout.visibility = View.VISIBLE
                    btnSendOtp.visibility = View.GONE
                    tvStatus.text = "OTP sent to $phone"
                    // Show OTP in dev mode
                    if (state.code != null) etOtp.setText(state.code)
                }
                is AuthState.Success -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AuthState.Error -> Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }
}
