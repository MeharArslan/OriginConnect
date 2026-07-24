package com.meharenterprises.originconnect.ui.main
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
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
    private lateinit var etSearch: EditText
    private lateinit var btnClear: ImageView

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
        fab.setOnClickListener {
            hideKeyboard()
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        etSearch = findViewById(R.id.etMainSearch)
        btnClear = findViewById(R.id.btnClearSearch)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString() ?: ""
                btnClear.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE
                getChatsFragment()?.search(q)
            }
        })

        btnClear.setOnClickListener {
            etSearch.setText("")
            btnClear.visibility = View.GONE
            getChatsFragment()?.search("")
            hideKeyboard()
        }

        navController.addOnDestinationChangedListener { _, dest, _ ->
            supportActionBar?.title = when (dest.id) {
                R.id.chatsFragment -> "OriginConnect"
                R.id.updatesFragment -> "Updates"
                R.id.communitiesFragment -> "Communities"
                R.id.callsFragment -> "Calls"
                else -> "OriginConnect"
            }
            etSearch.setText("")
            btnClear.visibility = View.GONE
            hideKeyboard()
        }
    }

    private fun getChatsFragment(): ChatsFragment? {
        val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment)
        return navHost?.childFragmentManager?.fragments?.firstOrNull() as? ChatsFragment
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
        etSearch.clearFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_camera -> {
            try {
                val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (i.resolveActivity(packageManager) != null) startActivity(i)
                else Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) { Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show() }
            true
        }
        R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
        R.id.action_profile  -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
        R.id.action_logout   -> {
            CoroutineScope(Dispatchers.Main).launch {
                session.clearSession()
                val i = Intent(this@MainActivity, AuthActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
            }; true
        }
        R.id.action_starred  -> { Toast.makeText(this, "Starred messages", Toast.LENGTH_SHORT).show(); true }
        R.id.action_archived -> { Toast.makeText(this, "Archived chats", Toast.LENGTH_SHORT).show(); true }
        else -> super.onOptionsItemSelected(item)
    }
}
