package com.meharenterprises.originconnect.ui.main
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.ui.chat.ChatActivity
import com.meharenterprises.originconnect.ui.main.adapter.ConversationAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val vm: MainViewModel by viewModels()
    @Inject lateinit var session: SessionManager
    private lateinit var adapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = ConversationAdapter { conv ->
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("CONVERSATION_ID", conv.id)
                putExtra("OTHER_USER_ID", conv.otherUserId)
            })
        }

        findViewById<RecyclerView>(R.id.recyclerConversations).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        findViewById<FloatingActionButton>(R.id.fabNewChat).setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        vm.conversations.observe(this) { adapter.submitList(it) }
        vm.loading.observe(this) { findViewById<ProgressBar>(R.id.progress).visibility = if (it) View.VISIBLE else View.GONE }

        vm.loadConversations()

        lifecycleScope.launch {
            val token = session.getAccessToken() ?: return@launch
            vm.connectSocket(token)
        }
    }

    override fun onResume() { super.onResume(); vm.loadConversations() }
}
