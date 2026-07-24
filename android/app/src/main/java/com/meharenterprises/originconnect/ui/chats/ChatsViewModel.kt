package com.meharenterprises.originconnect.ui.chats
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meharenterprises.originconnect.data.local.ContactNameCache
import com.meharenterprises.originconnect.data.model.Conversation
import com.meharenterprises.originconnect.data.remote.ApiService
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.data.socket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatsUiState(
    val conversations: List<Conversation> = emptyList(),
    val filtered: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val query: String = ""
)

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager,
    val nameCache: ContactNameCache,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _state = MutableStateFlow(ChatsUiState())
    val state: StateFlow<ChatsUiState> = _state.asStateFlow()

    init { socketManager.onNewMessage = { load() } }

    fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)
        try {
            val auth = session.getAuthHeader()
            // Populate name cache from registered contacts
            val contactsRes = api.getContacts(auth)
            contactsRes.body()?.forEach { c ->
                nameCache.putUserId(c.user.id, c.user.phone, c.user.displayName)
            }
            // Load conversations
            val list = api.getConversations(auth).body() ?: emptyList()
            _state.value = _state.value.copy(
                conversations = list,
                filtered = applyFilter(list, _state.value.query),
                isLoading = false
            )
        } catch (_: Exception) {
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun search(q: String) {
        _state.value = _state.value.copy(
            query = q,
            filtered = applyFilter(_state.value.conversations, q)
        )
    }

    fun connectSocket(token: String) {
        if (!socketManager.isConnected()) socketManager.connect(token)
    }

    private fun applyFilter(list: List<Conversation>, q: String): List<Conversation> {
        if (q.isBlank()) return list
        return list.filter {
            val name = nameCache.resolveUserId(it.otherUserId) ?: ""
            name.lowercase().contains(q.lowercase()) ||
            it.lastMessageContent?.lowercase()?.contains(q.lowercase()) == true
        }
    }
}
