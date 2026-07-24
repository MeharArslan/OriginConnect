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
        fun row(id: Int, action: () -> Unit) { try { findViewById<android.view.View>(id)?.setOnClickListener { action() } } catch (_: Exception) {} }
        fun msg(t: String) = Toast.makeText(this, t, Toast.LENGTH_SHORT).show()
        row(R.id.rowProfile)       { msg("Account settings coming soon") }
        row(R.id.rowPrivacy)       { msg("Privacy settings coming soon") }
        row(R.id.rowNotifications) { msg("Notification settings coming soon") }
        row(R.id.rowStorage)       { msg("Storage settings coming soon") }
        row(R.id.rowAppearance)    { msg("Appearance settings coming soon") }
        row(R.id.rowHelp)          { msg("Help & support coming soon") }
        row(R.id.rowLogout) {
            lifecycleScope.launch {
                session.clearSession()
                startActivity(Intent(this@SettingsActivity, AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
