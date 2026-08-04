package com.meharenterprises.originconnect.ui.main
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meharenterprises.originconnect.data.model.Contact
import com.meharenterprises.originconnect.data.remote.ApiService
import com.meharenterprises.originconnect.data.remote.SendMessageRequest
import com.meharenterprises.originconnect.data.remote.SyncContactsRequest
import com.meharenterprises.originconnect.data.local.OcContactDao
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
    private val session: SessionManager,
    private val contactDao: OcContactDao
) : ViewModel() {
    private val _contacts = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    val contacts: StateFlow<ContactsUiState> = _contacts.asStateFlow()
    private val _openChat = MutableStateFlow<OpenChatState>(OpenChatState.Idle)
    val openChat: StateFlow<OpenChatState> = _openChat.asStateFlow()

    // Called with device phone numbers to sync, then load
    fun syncAndLoad(devicePhones: List<String>) = viewModelScope.launch {
        _contacts.value = ContactsUiState.Loading
        // Show Room cache immediately while network loads
        val cached = contactDao.getAll()
        if (cached.isNotEmpty()) {
            val models = cached.map { e ->
                Contact(com.meharenterprises.originconnect.data.model.User(
                    e.userId, e.phone, e.localName ?: e.serverName, e.photoUrl, e.about
                ))
            }
            _contacts.value = ContactsUiState.Success(models, models)
        }
        try {
            val auth = session.getAuthHeader()
            if (devicePhones.isNotEmpty()) {
                api.syncContacts(SyncContactsRequest(devicePhones), auth)
            }
            val res = api.getContacts(auth)
            val list = if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
            if (list.isNotEmpty()) {
                _contacts.value = ContactsUiState.Success(list, list)
            } else if (cached.isEmpty()) {
                _contacts.value = ContactsUiState.Success(emptyList(), emptyList())
            }
        } catch (e: Exception) {
            if (cached.isEmpty()) _contacts.value = ContactsUiState.Err(e.message ?: "Network error")
        }
    }

    fun load() = viewModelScope.launch {
        _contacts.value = ContactsUiState.Loading
        val cached = contactDao.getAll()
        if (cached.isNotEmpty()) {
            val models = cached.map { e ->
                Contact(com.meharenterprises.originconnect.data.model.User(
                    e.userId, e.phone, e.localName ?: e.serverName, e.photoUrl, e.about
                ))
            }
            _contacts.value = ContactsUiState.Success(models, models)
        }
        try {
            val auth = session.getAuthHeader()
            val res = api.getContacts(auth)
            val list = if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
            if (list.isNotEmpty()) _contacts.value = ContactsUiState.Success(list, list)
            else if (cached.isEmpty()) _contacts.value = ContactsUiState.Success(emptyList(), emptyList())
        } catch (e: Exception) {
            if (cached.isEmpty()) _contacts.value = ContactsUiState.Err(e.message ?: "Network error")
        }
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
            _openChat.value = if (conv != null) OpenChatState.Ready(conv.id, userId)
                else OpenChatState.Err("Could not open chat")
        } catch (e: Exception) { _openChat.value = OpenChatState.Err(e.message ?: "Error") }
    }

    fun resetOpenChat() { _openChat.value = OpenChatState.Idle }
}
