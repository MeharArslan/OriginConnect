package com.meharenterprises.originconnect.ui.settings
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    @Inject lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_settings)

        val tb = findViewById<Toolbar>(R.id.settingsToolbar)
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        tb.setNavigationOnClickListener { finish() }

        findViewById<android.view.View>(R.id.rowProfile).setOnClickListener {
            Toast.makeText(this, "Account settings coming soon", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.rowPrivacy).setOnClickListener {
            Toast.makeText(this, "Privacy settings coming soon", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.rowNotifications).setOnClickListener {
            Toast.makeText(this, "Notification settings coming soon", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.rowStorage).setOnClickListener {
            Toast.makeText(this, "Storage settings coming soon", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.rowAppearance).setOnClickListener {
            Toast.makeText(this, "Appearance settings coming soon", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.rowHelp).setOnClickListener {
            Toast.makeText(this, "Help coming soon", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.rowLogout).setOnClickListener {
            lifecycleScope.launch {
                session.clearSession()
                val i = Intent(this@SettingsActivity, AuthActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
            }
        }
    }
}
