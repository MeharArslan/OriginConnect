package com.meharenterprises.originconnect.ui.auth
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    @Inject lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<android.widget.ImageView>(R.id.imgLogo)
        val name = findViewById<android.widget.TextView>(R.id.tvAppName)
        val tag = findViewById<android.widget.TextView>(R.id.tvTagline)
        val progress = findViewById<android.widget.ProgressBar>(R.id.progress)

        // Animate logo
        logo.animate().alpha(1f).scaleX(1.1f).scaleY(1.1f).setDuration(400).withEndAction {
            logo.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
        }.start()
        name.animate().alpha(1f).translationY(0f).setStartDelay(200).setDuration(400).start()
        tag.animate().alpha(1f).translationY(0f).setStartDelay(350).setDuration(400).start()
        progress.animate().alpha(1f).setStartDelay(600).setDuration(300).start()

        lifecycleScope.launch {
            delay(1500)
            val isLoggedIn = session.isLoggedIn()
            val dest = if (isLoggedIn) MainActivity::class.java else WelcomeActivity::class.java
            startActivity(Intent(this@SplashActivity, dest))
            overridePendingTransition(R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}
