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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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

        row(R.id.rowProfile) { startActivity(Intent(this, ProfileActivity::class.java)) }
        row(R.id.rowPrivacy) { toast("Privacy settings") }
        row(R.id.rowNotifications) { toast("Notification settings") }
        row(R.id.rowStorage) { toast("Storage settings") }
        row(R.id.rowAppearance) { toast("Appearance settings") }
        row(R.id.rowHelp) { toast("Help & FAQ") }
        row(R.id.rowLogout) { performLogout() }
    }

    private fun row(id: Int, action: () -> Unit) =
        try { findViewById<android.view.View>(id)?.setOnClickListener { action() } } catch (_: Exception) {}

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun performLogout() {
        // GlobalScope: not tied to activity lifecycle, won't crash on CLEAR_TASK
        GlobalScope.launch(Dispatchers.IO) {
            try { session.clearSession() } catch (_: Exception) {}
        }
        val i = Intent(this, WelcomeActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
