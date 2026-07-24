package com.meharenterprises.originconnect.ui.chat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
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
        val displayName = nameCache.resolveUserId(otherId) ?: ""
        val photoUrl    = nameCache.getPhotoUrl(otherId)

        val toolbar = findViewById<Toolbar>(R.id.chatToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = displayName.ifEmpty { "Chat" }

        // Show avatar in toolbar
        val imgToolbar = findViewById<ImageView>(R.id.imgChatAvatar)
        if (!photoUrl.isNullOrEmpty()) {
            imgToolbar.load(photoUrl) { transformations(CircleCropTransformation()) }
        }

        val adapter  = MessageAdapter(vm.myId)
        adapter.onDelete = { id, forEveryone -> vm.deleteMessage(id, forEveryone) }
        val recycler = findViewById<RecyclerView>(R.id.recyclerMessages)
        recycler.layoutManager = LinearLayoutManager(this).also { it.stackFromEnd = true }
        recycler.adapter = adapter

        val etMsg    = findViewById<EditText>(R.id.etMessage)
        val btnSend  = findViewById<ImageButton>(R.id.btnSend)
        val tvTyping = findViewById<TextView>(R.id.tvTyping)

        btnSend.setOnClickListener {
            val t = etMsg.text.toString().trim()
            if (t.isNotEmpty()) { vm.sendMessage(t); etMsg.setText("") }
        }
        etMsg.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { vm.sendTyping(!s.isNullOrEmpty()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        vm.messages.observe(this) { list ->
            adapter.myId = vm.myId; adapter.submitList(list)
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
