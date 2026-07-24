package com.meharenterprises.originconnect.ui.chat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.ContactNameCache
import com.meharenterprises.originconnect.ui.chat.adapter.MessageAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    private val vm: ChatViewModel by viewModels()
    @Inject lateinit var nameCache: ContactNameCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_chat)

        val convId  = intent.getStringExtra("CONVERSATION_ID") ?: ""
        val otherId = intent.getStringExtra("OTHER_USER_ID") ?: ""

        // Resolve display name: local contact name > backend name > last 8 chars
        val displayName = nameCache.resolveUserId(otherId)
            ?: otherId.takeLast(8)

        val toolbar = findViewById<Toolbar>(R.id.chatToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = displayName

        val adapter = MessageAdapter("")
        val recycler = findViewById<RecyclerView>(R.id.recyclerMessages)
        recycler.layoutManager = LinearLayoutManager(this).also { it.stackFromEnd = true }
        recycler.adapter = adapter

        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend   = findViewById<ImageButton>(R.id.btnSend)
        val tvTyping  = findViewById<TextView>(R.id.tvTyping)

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) { vm.sendMessage(text); etMessage.setText("") }
        }
        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { vm.sendTyping(!s.isNullOrEmpty()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        vm.messages.observe(this) { list ->
            adapter.myId = vm.myId
            adapter.submitList(list)
            if (list.isNotEmpty()) recycler.scrollToPosition(list.size - 1)
        }
        vm.typing.observe(this) { tvTyping.visibility = if (it) View.VISIBLE else View.GONE }
        vm.init(convId, otherId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
