package com.meharenterprises.originconnect.ui.main
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.ui.chat.ChatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsActivity : AppCompatActivity() {
    private val vm: ContactsViewModel by viewModels()

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) readContactsAndSync() else vm.load() // try without device contacts
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_contacts)

        val tb = findViewById<Toolbar>(R.id.contactsToolbar)
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select contact"
        tb.setNavigationOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerContacts)
        val etSearch = findViewById<EditText>(R.id.etContactSearch)
        val progress = findViewById<ProgressBar>(R.id.contactsProgress)
        val tvEmpty  = findViewById<TextView>(R.id.tvContactsEmpty)

        val adapter = ContactsAdapter { contact -> vm.openConversation(contact.user.id) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) { vm.filter(s.toString()) }
        })

        lifecycleScope.launch {
            vm.contacts.collectLatest { state ->
                when (state) {
                    is ContactsUiState.Loading -> {
                        progress.visibility = View.VISIBLE
                        recycler.visibility = View.GONE
                        tvEmpty.visibility = View.GONE
                    }
                    is ContactsUiState.Success -> {
                        progress.visibility = View.GONE
                        if (state.filtered.isEmpty()) {
                            recycler.visibility = View.GONE
                            tvEmpty.visibility = View.VISIBLE
                            tvEmpty.text = if (state.all.isEmpty())
                                "No OriginConnect users found in your contacts. Share OriginConnect with friends to get started."
                            else "No contacts match your search."
                        } else {
                            tvEmpty.visibility = View.GONE
                            recycler.visibility = View.VISIBLE
                            adapter.submitList(state.filtered)
                        }
                    }
                    is ContactsUiState.Err -> {
                        progress.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = "Error: ${state.msg}. Tap to retry."
                        tvEmpty.setOnClickListener { requestContactsPermission() }
                    }
                }
            }
        }

        lifecycleScope.launch {
            vm.openChat.collectLatest { state ->
                when (state) {
                    is OpenChatState.Loading -> progress.visibility = View.VISIBLE
                    is OpenChatState.Ready   -> {
                        progress.visibility = View.GONE
                        startActivity(Intent(this@ContactsActivity, ChatActivity::class.java).apply {
                            putExtra("CONVERSATION_ID", state.conversationId)
                            putExtra("OTHER_USER_ID", state.otherUserId)
                        })
                        vm.resetOpenChat()
                        finish()
                    }
                    is OpenChatState.Err -> {
                        progress.visibility = View.GONE
                        Toast.makeText(this@ContactsActivity, state.msg, Toast.LENGTH_SHORT).show()
                        vm.resetOpenChat()
                    }
                    else -> progress.visibility = View.GONE
                }
            }
        }

        requestContactsPermission()
    }

    private fun requestContactsPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED -> readContactsAndSync()
            else -> requestPermission.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun readContactsAndSync() {
        val phones = mutableListOf<String>()
        try {
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, null
            )
            cursor?.use {
                val col = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val raw = it.getString(col)?.trim() ?: continue
                    // Normalize: remove spaces/dashes, ensure starts with +
                    val normalized = raw.replace("[\s\-().]".toRegex(), "")
                    if (normalized.isNotEmpty()) phones.add(normalized)
                }
            }
        } catch (_: Exception) {}

        // Also add without country code variants for Pakistan (+92 → 0)
        val allPhones = phones.toMutableSet()
        phones.forEach { p ->
            if (p.startsWith("+92") && p.length > 3) allPhones.add("0" + p.drop(3))
            if (p.startsWith("0") && p.length > 1) allPhones.add("+92" + p.drop(1))
        }

        vm.syncAndLoad(allPhones.toList())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
