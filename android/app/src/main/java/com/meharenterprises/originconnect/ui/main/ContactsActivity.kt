package com.meharenterprises.originconnect.ui.main
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_contacts)

        val tb = findViewById<Toolbar>(R.id.contactsToolbar)
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select contact"
        tb.setNavigationOnClickListener { finish() }

        val recycler   = findViewById<RecyclerView>(R.id.recyclerContacts)
        val etSearch   = findViewById<EditText>(R.id.etContactSearch)
        val progress   = findViewById<ProgressBar>(R.id.contactsProgress)
        val tvEmpty    = findViewById<TextView>(R.id.tvContactsEmpty)

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
                    is ContactsUiState.Loading -> { progress.visibility = View.VISIBLE; recycler.visibility = View.GONE; tvEmpty.visibility = View.GONE }
                    is ContactsUiState.Success -> {
                        progress.visibility = View.GONE
                        if (state.filtered.isEmpty()) {
                            recycler.visibility = View.GONE; tvEmpty.visibility = View.VISIBLE
                            tvEmpty.text = if (state.all.isEmpty()) "No contacts on OriginConnect yet.
Share your number with friends." else "No results."
                        } else { tvEmpty.visibility = View.GONE; recycler.visibility = View.VISIBLE; adapter.submitList(state.filtered) }
                    }
                    is ContactsUiState.Err -> { progress.visibility = View.GONE; tvEmpty.visibility = View.VISIBLE; tvEmpty.text = state.msg }
                }
            }
        }

        lifecycleScope.launch {
            vm.openChat.collectLatest { s ->
                when (s) {
                    is OpenChatState.Loading -> progress.visibility = View.VISIBLE
                    is OpenChatState.Ready -> {
                        progress.visibility = View.GONE
                        startActivity(Intent(this@ContactsActivity, ChatActivity::class.java).apply {
                            putExtra("CONVERSATION_ID", s.conversationId)
                            putExtra("OTHER_USER_ID", s.otherUserId)
                        })
                        vm.resetOpenChat(); finish()
                    }
                    is OpenChatState.Err -> { progress.visibility = View.GONE; Toast.makeText(this@ContactsActivity, s.msg, Toast.LENGTH_SHORT).show(); vm.resetOpenChat() }
                    else -> progress.visibility = View.GONE
                }
            }
        }
        vm.load()
    }
}
