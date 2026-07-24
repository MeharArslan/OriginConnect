package com.meharenterprises.originconnect.ui.main
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
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
    private lateinit var fab: ExtendedFloatingActionButton

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

        navController.addOnDestinationChangedListener { _, dest, _ ->
            when (dest.id) {
                R.id.chatsFragment -> {
                    fab.show(); fab.text = "New message"; fab.setIconResource(R.drawable.ic_chat)
                    fab.setOnClickListener { startActivity(Intent(this, ContactsActivity::class.java)) }
                    supportActionBar?.title = "OriginConnect"
                }
                R.id.updatesFragment -> {
                    fab.show(); fab.text = "Add update"; fab.setIconResource(R.drawable.ic_camera)
                    fab.setOnClickListener { startActivity(Intent(this, ContactsActivity::class.java)) }
                    supportActionBar?.title = "Updates"
                }
                R.id.communitiesFragment -> {
                    fab.show(); fab.text = "New community"; fab.setIconResource(R.drawable.ic_communities)
                    fab.setOnClickListener { startActivity(Intent(this, ContactsActivity::class.java)) }
                    supportActionBar?.title = "Communities"
                }
                R.id.callsFragment -> {
                    fab.show(); fab.text = "New call"; fab.setIconResource(R.drawable.ic_calls)
                    fab.setOnClickListener { startActivity(Intent(this, ContactsActivity::class.java)) }
                    supportActionBar?.title = "Calls"
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val sv = menu.findItem(R.id.action_search)?.actionView as? SearchView
        sv?.queryHint = "Search chats..."
        sv?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = true
            override fun onQueryTextChange(q: String?): Boolean {
                val frag = supportFragmentManager.findFragmentById(R.id.navHostFragment)
                    ?.childFragmentManager?.fragments?.firstOrNull()
                if (frag is ChatsFragment) frag.search(q ?: "")
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
        R.id.action_profile  -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
        R.id.action_logout   -> {
            CoroutineScope(Dispatchers.Main).launch {
                session.clearSession()
                startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                finishAffinity()
            }; true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
