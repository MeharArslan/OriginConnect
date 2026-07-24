package com.meharenterprises.originconnect.ui.settings
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.ui.auth.WelcomeActivity
import com.meharenterprises.originconnect.ui.profile.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
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

        click(R.id.rowProfile)       { startActivity(Intent(this, ProfileActivity::class.java)) }
        click(R.id.rowPrivacy)       { toast("Privacy settings coming soon") }
        click(R.id.rowNotifications) { toast("Notification settings coming soon") }
        click(R.id.rowStorage)       { toast("Storage settings coming soon") }
        click(R.id.rowAppearance)    { toast("Appearance settings coming soon") }
        click(R.id.rowHelp)          { toast("Help & support coming soon") }
        click(R.id.rowLogout)        { doLogout() }
    }

    private fun click(id: Int, action: () -> Unit) {
        try { findViewById<android.view.View>(id)?.setOnClickListener { action() } } catch (_: Exception) {}
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // Use GlobalScope so coroutine doesn't get cancelled when activity is cleared
    private fun doLogout() {
        GlobalScope.launch(Dispatchers.IO) {
            try { session.clearSession() } catch (_: Exception) {}
            val intent = Intent(this@SettingsActivity, WelcomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
