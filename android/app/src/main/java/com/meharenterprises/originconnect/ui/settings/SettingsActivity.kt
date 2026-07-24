package com.meharenterprises.originconnect.ui.settings
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.ui.auth.WelcomeActivity
import com.meharenterprises.originconnect.ui.profile.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
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

        row(R.id.rowProfile)       { startActivity(Intent(this, ProfileActivity::class.java)) }
        row(R.id.rowPrivacy)       { toast("Privacy — coming soon") }
        row(R.id.rowNotifications) { toast("Notifications — coming soon") }
        row(R.id.rowStorage)       { toast("Storage — coming soon") }
        row(R.id.rowAppearance)    { toast("Appearance — coming soon") }
        row(R.id.rowHelp)          { toast("Help — coming soon") }
        row(R.id.rowLogout)        { doLogout() }
    }

    private fun row(id: Int, action: () -> Unit) =
        try { findViewById<android.view.View>(id)?.setOnClickListener { action() } } catch (_: Exception) {}

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()

    private fun doLogout() {
        // Clear session on IO thread, then navigate on main thread via Handler
        // Handler ensures we don't touch destroyed activity
        val handler = Handler(Looper.getMainLooper())
        CoroutineScope(Dispatchers.IO).launch {
            try { session.clearSession() } catch (_: Exception) {}
            handler.post {
                try {
                    val i = Intent(applicationContext, WelcomeActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    applicationContext.startActivity(i)
                } catch (_: Exception) {}
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
