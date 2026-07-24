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

        val rowTitles = mapOf(
            R.id.rowProfile to "Account",
            R.id.rowPrivacy to "Privacy",
            R.id.rowNotifications to "Notifications",
            R.id.rowStorage to "Storage and data",
            R.id.rowAppearance to "Appearance",
            R.id.rowHelp to "Help",
            R.id.rowLogout to "Log out"
        )
        rowTitles.forEach { (id, title) ->
            findViewById<android.widget.TextView>(id)
                ?.parent?.let { (it as? android.view.ViewGroup)?.findViewById<android.widget.TextView>(R.id.tvSettingsTitle) }
                ?.text = title
        }
        val tb = findViewById<Toolbar>(R.id.settingsToolbar)
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        tb.setNavigationOnClickListener { finish() }

        findViewById<android.view.View>(R.id.rowProfile).setOnClickListener {
            Toast.makeText(this, "Edit Profile", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.rowPrivacy).setOnClickListener {
            Toast.makeText(this, "Privacy Settings", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.rowNotifications).setOnClickListener {
            Toast.makeText(this, "Notification Settings", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.rowStorage).setOnClickListener {
            Toast.makeText(this, "Storage & Data", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.rowAppearance).setOnClickListener {
            Toast.makeText(this, "Appearance", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.rowHelp).setOnClickListener {
            Toast.makeText(this, "Help & Support", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.view.View>(R.id.rowLogout).setOnClickListener {
            lifecycleScope.launch {
                session.clearSession()
                startActivity(Intent(this@SettingsActivity, AuthActivity::class.java))
                finishAffinity()
            }
        }
    }
}
