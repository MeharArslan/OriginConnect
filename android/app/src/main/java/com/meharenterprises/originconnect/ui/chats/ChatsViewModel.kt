package com.meharenterprises.originconnect.ui.chats
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meharenterprises.originconnect.data.local.ContactNameCache
import com.meharenterprises.originconnect.data.local.OcContactDao
import com.meharenterprises.originconnect.data.local.OcContactEntity
import com.meharenterprises.originconnect.data.local.OcConversationDao
import com.meharenterprises.originconnect.data.local.OcConversationEntity
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.data.model.Conversation
import com.meharenterprises.originconnect.data.remote.ApiService
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
    private val conversationDao: OcConversationDao,
    private val contactDao: OcContactDao,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _state = MutableStateFlow(ChatsUiState())
    val state: StateFlow<ChatsUiState> = _state.asStateFlow()

    init {
        socketManager.onNewMessage = { load() }
        viewModelScope.launch {
            val cachedContacts = contactDao.getAll()
            if (cachedContacts.isNotEmpty()) nameCache.preloadFromRoom(cachedContacts)
            val cached = conversationDao.getAll().map { it.toConversation() }
            if (cached.isNotEmpty()) {
                _state.value = _state.value.copy(
                    conversations = cached,
                    filtered = applyFilter(cached, ""),
                    isLoading = true
                )
            }
        }
    }

    fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)
        try {
            val auth = session.getAuthHeader()
            val contacts = api.getContacts(auth).body() ?: emptyList()
            contacts.forEach { c ->
                nameCache.putUserId(c.user.id, c.user.phone, c.user.displayName, c.user.photoUrl)
            }
            if (contacts.isNotEmpty()) {
                contactDao.upsertAll(contacts.map { c ->
                    OcContactEntity(c.user.id, c.user.phone, c.user.displayName,
                        nameCache.resolvePhone(c.user.phone), c.user.photoUrl, c.user.about)
                })
            }
            val list = api.getConversations(auth).body() ?: emptyList()
            conversationDao.upsertAll(list.map { it.toEntity() })
            _state.value = _state.value.copy(
                conversations = list,
                filtered = applyFilter(list, _state.value.query),
                isLoading = false
            )
        } catch (_: Exception) {
            val fallback = conversationDao.getAll().map { it.toConversation() }
            _state.value = _state.value.copy(
                conversations = fallback,
                filtered = applyFilter(fallback, _state.value.query),
                isLoading = false
            )
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

    fun deleteLocalConversation(convId: String) = viewModelScope.launch {
        conversationDao.delete(convId)
        val updated = _state.value.conversations.filter { it.id != convId }
        _state.value = _state.value.copy(
            conversations = updated,
            filtered = applyFilter(updated, _state.value.query)
        )
    }

    private fun applyFilter(list: List<Conversation>, q: String): List<Conversation> {
        if (q.isBlank()) return list
        val lower = q.lowercase()
        return list.filter {
            (nameCache.resolveUserId(it.otherUserId) ?: "").lowercase().contains(lower) ||
            it.lastMessageContent?.lowercase()?.contains(lower) == true
        }
    }
}

private fun Conversation.toEntity() = OcConversationEntity(
    id, otherUserId, lastMessageContent, lastMessageAt, unreadCount, participant1Id, participant2Id
)

private fun OcConversationEntity.toConversation() = Conversation(
    id, otherUserId, lastMessageContent, lastMessageAt, unreadCount, participant1Id, participant2Id
)
