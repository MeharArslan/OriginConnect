package com.meharenterprises.originconnect.ui.chat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.ui.chat.adapter.MessageAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    private val vm: ChatViewModel by viewModels()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val convId = intent.getStringExtra("CONVERSATION_ID") ?: ""
        val otherId = intent.getStringExtra("OTHER_USER_ID") ?: ""
        title = otherId.take(8)
        adapter = MessageAdapter("")
        val recycler = findViewById<RecyclerView>(R.id.recyclerMessages)
        recycler.layoutManager = LinearLayoutManager(this).also { it.stackFromEnd = true }
        recycler.adapter = adapter
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<ImageButton>(R.id.btnSend)
        val tvTyping = findViewById<TextView>(R.id.tvTyping)
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) { vm.sendMessage(text); etMessage.setText("") }
        }
        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { vm.sendTyping(s?.isNotEmpty() == true) }
            override fun afterTextChanged(s: Editable?) {}
        })
        vm.messages.observe(this) {
            adapter.myId = vm.myId
            adapter.submitList(it)
            if (it.isNotEmpty()) recycler.scrollToPosition(it.size - 1)
        }
        vm.typing.observe(this) { tvTyping.visibility = if (it) View.VISIBLE else View.GONE }
        vm.init(convId, otherId)
    }
}
