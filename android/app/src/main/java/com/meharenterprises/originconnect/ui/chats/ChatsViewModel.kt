package com.meharenterprises.originconnect.ui.chats
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meharenterprises.originconnect.data.model.Conversation
import com.meharenterprises.originconnect.data.repository.ChatRepository
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
    private val chatRepo: ChatRepository,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _state = MutableStateFlow(ChatsUiState())
    val state: StateFlow<ChatsUiState> = _state.asStateFlow()

    init {
        socketManager.onNewMessage = { loadConversations() }
    }

    fun load() = loadConversations()

    private fun loadConversations() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val list = chatRepo.getConversations()
            _state.value = _state.value.copy(
                conversations = list,
                filtered = applyFilter(list, _state.value.query),
                isLoading = false
            )
        }
    }

    fun search(query: String) {
        val q = query.trim()
        _state.value = _state.value.copy(
            query = q,
            filtered = applyFilter(_state.value.conversations, q)
        )
    }

    fun connectSocket(token: String) {
        if (!socketManager.isConnected()) socketManager.connect(token)
    }

    private fun applyFilter(list: List<Conversation>, query: String): List<Conversation> {
        if (query.isBlank()) return list
        val q = query.lowercase()
        return list.filter {
            it.otherUserId.lowercase().contains(q) ||
            it.lastMessageContent?.lowercase()?.contains(q) == true
        }
    }
}
