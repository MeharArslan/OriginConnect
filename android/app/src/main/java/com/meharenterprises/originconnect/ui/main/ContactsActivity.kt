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

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) syncDeviceContacts() else vm.load() }

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
                                "No contacts found on OriginConnect. Invite friends to join."
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
                        tvEmpty.text = "Could not load contacts. Check connection."
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED) {
            syncDeviceContacts()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun syncDeviceContacts() {
        val phones = mutableSetOf<String>()
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
                    // Remove all non-digit chars except leading +
                    val digits = raw.filter { c -> c.isDigit() || c == '+' }
                        .trimStart()
                    if (digits.length >= 7) {
                        phones.add(digits)
                        // Add E.164 variants for Pakistan
                        when {
                            digits.startsWith("+92") -> phones.add("0" + digits.drop(3))
                            digits.startsWith("0") && digits.length >= 10 -> phones.add("+92" + digits.drop(1))
                            digits.length == 10 -> phones.add("+92$digits")
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        vm.syncAndLoad(phones.toList())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
