package com.meharenterprises.originconnect.ui.main
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.ui.auth.AuthActivity
import com.meharenterprises.originconnect.ui.chats.ChatsFragment
import com.meharenterprises.originconnect.ui.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var session: SessionManager
    private lateinit var navController: NavController
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "OriginConnect"

        val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHost.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)

        fab = findViewById(R.id.fab)
        fab.setOnClickListener { startActivity(Intent(this, ContactsActivity::class.java)) }

        // Wire search bar
        val etSearch = findViewById<EditText>(R.id.etMainSearch)
        val btnClear = findViewById<ImageView>(R.id.btnClearSearch)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString()
                btnClear.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE
                getCurrentChatsFragment()?.search(q)
            }
        })
        btnClear.setOnClickListener {
            etSearch.setText("")
            btnClear.visibility = View.GONE
            getCurrentChatsFragment()?.search("")
        }

        navController.addOnDestinationChangedListener { _, dest, _ ->
            supportActionBar?.title = when (dest.id) {
                R.id.chatsFragment       -> "OriginConnect"
                R.id.updatesFragment     -> "Updates"
                R.id.communitiesFragment -> "Communities"
                R.id.callsFragment       -> "Calls"
                else -> "OriginConnect"
            }
        }
    }

    private fun getCurrentChatsFragment(): ChatsFragment? {
        val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment)
        return navHost?.childFragmentManager?.fragments?.firstOrNull() as? ChatsFragment
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_camera   -> true  // camera handled by FAB context
        R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
        R.id.action_starred  -> true
        R.id.action_archived -> true
        R.id.action_profile  -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
        R.id.action_logout   -> {
            CoroutineScope(Dispatchers.Main).launch {
                session.clearSession()
                val i = Intent(this@MainActivity, AuthActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
            }; true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
