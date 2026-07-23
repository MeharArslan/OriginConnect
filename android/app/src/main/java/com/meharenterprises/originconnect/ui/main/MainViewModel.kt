package com.meharenterprises.originconnect.ui.main
import androidx.lifecycle.*
import com.meharenterprises.originconnect.data.model.Conversation
import com.meharenterprises.originconnect.data.repository.ChatRepository
import com.meharenterprises.originconnect.data.socket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _conversations = MutableLiveData<List<Conversation>>(emptyList())
    val conversations: LiveData<List<Conversation>> = _conversations

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun loadConversations() {
        viewModelScope.launch {
            _loading.value = true
            _conversations.value = chatRepo.getConversations()
            _loading.value = false
        }
    }

    fun connectSocket(token: String) {
        if (!socketManager.isConnected()) {
            socketManager.connect(token)
            socketManager.onNewMessage = { loadConversations() }
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
    }
}
