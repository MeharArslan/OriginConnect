package com.meharenterprises.originconnect.ui.auth
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpActivity : AppCompatActivity() {
    private val vm: AuthViewModel by viewModels()
    private var phone = ""
    private var timer: CountDownTimer? = null
    private lateinit var otpBoxes: List<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        phone = intent.getStringExtra("PHONE") ?: ""
        val devCode = intent.getStringExtra("CODE")

        val tvPhone = findViewById<TextView>(R.id.tvPhone)
        val tvError = findViewById<TextView>(R.id.tvError)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val tvResendTimer = findViewById<TextView>(R.id.tvResendTimer)
        val tvResend = findViewById<TextView>(R.id.tvResend)
        val tvWrong = findViewById<TextView>(R.id.tvWrongNumber)

        tvPhone.text = phone
        otpBoxes = listOf(
            findViewById(R.id.otp1), findViewById(R.id.otp2), findViewById(R.id.otp3),
            findViewById(R.id.otp4), findViewById(R.id.otp5), findViewById(R.id.otp6)
        )

        // Auto-fill dev code
        if (!devCode.isNullOrEmpty() && devCode.length == 6) {
            devCode.forEachIndexed { i, c -> otpBoxes[i].setText(c.toString()) }
            verifyOtp()
        }

        setupOtpBoxes(tvError)
        startResendTimer(tvResendTimer, tvResend)

        tvResend.setOnClickListener {
            vm.sendOtp(phone)
            tvResend.visibility = View.GONE
            startResendTimer(tvResendTimer, tvResend)
        }

        tvWrong.setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        vm.state.observe(this) { state ->
            progress.visibility = if (state is AuthState.Loading) View.VISIBLE else View.GONE
            when (state) {
                is AuthState.Success -> {
                    if (state.isNewUser) {
                        startActivity(Intent(this, ProfileSetupActivity::class.java))
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    finishAffinity()
                }
                is AuthState.Error -> {
                    tvError.text = state.message
                    tvError.visibility = View.VISIBLE
                    otpBoxes.forEach { it.setBackgroundResource(R.drawable.bg_otp_error) }
                }
                else -> {}
            }
        }
    }

    private fun setupOtpBoxes(tvError: TextView) {
        otpBoxes.forEachIndexed { i, box ->
            box.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    tvError.visibility = View.GONE
                    otpBoxes.forEach { it.setBackgroundResource(R.drawable.bg_otp_box) }
                    if (s?.length == 1 && i < 5) otpBoxes[i + 1].requestFocus()
                    if (otpBoxes.all { it.text.length == 1 }) verifyOtp()
                }
            })
            box.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && box.text.isEmpty() && i > 0) {
                    otpBoxes[i - 1].apply { setText(""); requestFocus() }
                    true
                } else false
            }
        }
        otpBoxes.first().requestFocus()
    }

    private fun verifyOtp() {
        val code = otpBoxes.joinToString("") { it.text.toString() }
        if (code.length == 6) vm.verifyOtp(phone, code)
    }

    private fun startResendTimer(tvTimer: TextView, tvResend: TextView) {
        timer?.cancel()
        tvResend.visibility = View.GONE
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(ms: Long) {
                tvTimer.text = getString(R.string.resend_in, ms / 1000)
            }
            override fun onFinish() {
                tvTimer.text = ""
                tvResend.visibility = View.VISIBLE
            }
        }.start()
    }

    override fun onDestroy() { super.onDestroy(); timer?.cancel() }
}
