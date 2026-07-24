package com.meharenterprises.originconnect.ui.main
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meharenterprises.originconnect.data.model.Contact
import com.meharenterprises.originconnect.data.remote.ApiService
import com.meharenterprises.originconnect.data.remote.SendMessageRequest
import com.meharenterprises.originconnect.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ContactsUiState {
    object Loading : ContactsUiState()
    data class Success(val all: List<Contact>, val filtered: List<Contact>) : ContactsUiState()
    data class Err(val msg: String) : ContactsUiState()
}
sealed class OpenChatState {
    object Idle : OpenChatState()
    object Loading : OpenChatState()
    data class Ready(val conversationId: String, val otherUserId: String) : OpenChatState()
    data class Err(val msg: String) : OpenChatState()
}

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager
) : ViewModel() {
    private val _contacts = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    val contacts: StateFlow<ContactsUiState> = _contacts.asStateFlow()
    private val _openChat = MutableStateFlow<OpenChatState>(OpenChatState.Idle)
    val openChat: StateFlow<OpenChatState> = _openChat.asStateFlow()

    fun load() = viewModelScope.launch {
        _contacts.value = ContactsUiState.Loading
        try {
            val r = api.getContacts(session.getAuthHeader())
            val list = if (r.isSuccessful) r.body() ?: emptyList() else emptyList()
            _contacts.value = ContactsUiState.Success(list, list)
        } catch (e: Exception) { _contacts.value = ContactsUiState.Err(e.message ?: "Network error") }
    }

    fun filter(q: String) {
        val s = _contacts.value as? ContactsUiState.Success ?: return
        _contacts.value = s.copy(filtered = if (q.isBlank()) s.all
            else s.all.filter { it.user.displayName.lowercase().contains(q.lowercase()) || it.user.phone.contains(q) })
    }

    fun openConversation(userId: String) = viewModelScope.launch {
        _openChat.value = OpenChatState.Loading
        try {
            val auth = session.getAuthHeader()
            val existing = api.getConversations(auth).body()?.firstOrNull { it.otherUserId == userId }
            if (existing != null) { _openChat.value = OpenChatState.Ready(existing.id, userId); return@launch }
            api.sendMessage(SendMessageRequest(userId, "text", "👋"), auth)
            val conv = api.getConversations(auth).body()?.firstOrNull { it.otherUserId == userId }
            _openChat.value = if (conv != null) OpenChatState.Ready(conv.id, userId) else OpenChatState.Err("Could not open chat")
        } catch (e: Exception) { _openChat.value = OpenChatState.Err(e.message ?: "Error") }
    }

    fun resetOpenChat() { _openChat.value = OpenChatState.Idle }
}
