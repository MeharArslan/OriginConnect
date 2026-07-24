package com.meharenterprises.originconnect.ui.main
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.ui.chats.ChatsFragment
import com.meharenterprises.originconnect.ui.profile.ProfileActivity
import com.meharenterprises.originconnect.ui.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var session: SessionManager
    private lateinit var navController: NavController
    private lateinit var fab: FloatingActionButton
    private lateinit var etSearch: EditText
    private lateinit var btnClear: ImageView

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { /* bitmap captured, handle if needed */ }

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
                R.id.chatsFragment       -> "OriginConnect"
                R.id.updatesFragment     -> "Updates"
                R.id.communitiesFragment -> "Communities"
                R.id.callsFragment       -> "Calls"
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

    private fun launchCamera() = cameraLauncher.launch(null)

    private fun openCamera() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_camera   -> { openCamera(); true }
        R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
        R.id.action_profile  -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
        R.id.action_archived -> { Toast.makeText(this, "No archived chats", Toast.LENGTH_SHORT).show(); true }
        else -> super.onOptionsItemSelected(item)
    }
}
