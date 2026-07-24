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

sealed class ContactsState {
    object Loading : ContactsState()
    data class Success(val contacts: List<Contact>, val filtered: List<Contact>) : ContactsState()
    data class Error(val message: String) : ContactsState()
}

sealed class OpenChatResult {
    object Idle : OpenChatResult()
    object Loading : OpenChatResult()
    data class Ready(val conversationId: String, val otherUserId: String) : OpenChatResult()
    data class Error(val msg: String) : OpenChatResult()
}

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager
) : ViewModel() {
    private val _state = MutableStateFlow<ContactsState>(ContactsState.Loading)
    val state: StateFlow<ContactsState> = _state.asStateFlow()
    private val _openChat = MutableStateFlow<OpenChatResult>(OpenChatResult.Idle)
    val openChat: StateFlow<OpenChatResult> = _openChat.asStateFlow()

    fun loadContacts() {
        viewModelScope.launch {
            _state.value = ContactsState.Loading
            try {
                val res = api.getContacts(session.getAuthHeader())
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    _state.value = ContactsState.Success(list, list)
                } else _state.value = ContactsState.Error("Failed to load contacts")
            } catch (e: Exception) { _state.value = ContactsState.Error(e.message ?: "Network error") }
        }
    }

    fun filter(query: String) {
        val s = _state.value as? ContactsState.Success ?: return
        val q = query.lowercase()
        _state.value = s.copy(filtered = if (q.isBlank()) s.contacts
            else s.contacts.filter { it.user.displayName.lowercase().contains(q) || it.user.phone.contains(q) })
    }

    fun openConversation(contactUserId: String) {
        _openChat.value = OpenChatResult.Loading
        viewModelScope.launch {
            try {
                val auth = session.getAuthHeader()
                val existing = api.getConversations(auth).body()?.firstOrNull { it.otherUserId == contactUserId }
                if (existing != null) { _openChat.value = OpenChatResult.Ready(existing.id, contactUserId); return@launch }
                val msgRes = api.sendMessage(SendMessageRequest(contactUserId, "text", "👋"), auth)
                if (msgRes.isSuccessful) {
                    val conv = api.getConversations(auth).body()?.firstOrNull { it.otherUserId == contactUserId }
                    _openChat.value = if (conv != null) OpenChatResult.Ready(conv.id, contactUserId)
                        else OpenChatResult.Error("Could not create conversation")
                } else _openChat.value = OpenChatResult.Error("Failed to open chat")
            } catch (e: Exception) { _openChat.value = OpenChatResult.Error(e.message ?: "Error") }
        }
    }
    fun resetOpenChat() { _openChat.value = OpenChatResult.Idle }
}
