package com.meharenterprises.originconnect.ui.main
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.ui.auth.AuthActivity
import com.meharenterprises.originconnect.ui.chats.ChatsFragment
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

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHost.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)

        fab = findViewById(R.id.fab)

        // Dynamic FAB per tab
        navController.addOnDestinationChangedListener { _, dest, _ ->
            when (dest.id) {
                R.id.chatsFragment -> {
                    fab.show()
                    fab.text = "New message"
                    fab.setIconResource(R.drawable.ic_chat)
                    fab.setOnClickListener { startActivity(Intent(this, ContactsActivity::class.java)) }
                    toolbar.title = "OriginConnect"
                }
                R.id.updatesFragment -> {
                    fab.show()
                    fab.text = "Add update"
                    fab.setIconResource(R.drawable.ic_camera)
                    fab.setOnClickListener { Toast.makeText(this, "Add status", Toast.LENGTH_SHORT).show() }
                    toolbar.title = "Updates"
                }
                R.id.communitiesFragment -> {
                    fab.show()
                    fab.text = "New community"
                    fab.setIconResource(R.drawable.ic_communities)
                    fab.setOnClickListener { Toast.makeText(this, "New community", Toast.LENGTH_SHORT).show() }
                    toolbar.title = "Communities"
                }
                R.id.callsFragment -> {
                    fab.show()
                    fab.text = "New call"
                    fab.setIconResource(R.drawable.ic_calls)
                    fab.setOnClickListener { Toast.makeText(this, "New call", Toast.LENGTH_SHORT).show() }
                    toolbar.title = "Calls"
                }
            }
        }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_camera -> { Toast.makeText(this, "Camera", Toast.LENGTH_SHORT).show(); true }
                R.id.action_settings -> { Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show(); true }
                R.id.action_starred -> { Toast.makeText(this, "Starred Messages", Toast.LENGTH_SHORT).show(); true }
                R.id.action_archived -> { Toast.makeText(this, "Archived", Toast.LENGTH_SHORT).show(); true }
                R.id.action_profile -> { Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show(); true }
                R.id.action_logout -> { logout(); true }
                else -> false
            }
        }

        // SearchView wired to ChatsFragment
        toolbar.post {
            val searchItem = toolbar.menu?.findItem(R.id.action_search)
            val searchView = searchItem?.actionView as? SearchView
            searchView?.queryHint = "Search chats..."
            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(q: String?) = true
                override fun onQueryTextChange(q: String?): Boolean {
                    val chatsFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)
                        ?.childFragmentManager?.fragments?.firstOrNull() as? ChatsFragment
                    chatsFragment?.search(q ?: "")
                    return true
                }
            })
        }
    }

    private fun logout() {
        CoroutineScope(Dispatchers.Main).launch {
            session.clearSession()
            startActivity(Intent(this@MainActivity, AuthActivity::class.java))
            finishAffinity()
        }
    }
}
