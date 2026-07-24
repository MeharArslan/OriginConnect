package com.meharenterprises.originconnect.ui.chats
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.ui.chat.ChatActivity
import com.meharenterprises.originconnect.ui.main.adapter.ConversationAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatsFragment : Fragment() {
    private val vm: ChatsViewModel by viewModels()
    @Inject lateinit var session: SessionManager
    private lateinit var adapter: ConversationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_chats, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerChats)
        val swipe = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)

        adapter = ConversationAdapter { conv ->
            startActivity(Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("CONVERSATION_ID", conv.id)
                putExtra("OTHER_USER_ID", conv.otherUserId)
            })
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        swipe.setOnRefreshListener { vm.load() }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.state.collectLatest { s ->
                adapter.submitList(s.filtered)
                swipe.isRefreshing = s.isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val token = session.getAccessToken() ?: return@launch
            vm.connectSocket(token)
        }

        vm.load()
    }

    override fun onResume() { super.onResume(); vm.load() }

    fun search(query: String) = vm.search(query)
}
