package com.meharenterprises.originconnect.ui.chat
import androidx.lifecycle.*
import com.meharenterprises.originconnect.data.local.OcMessageDao
import com.meharenterprises.originconnect.data.model.Message
import com.meharenterprises.originconnect.data.repository.ChatRepository
import com.meharenterprises.originconnect.data.socket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val messageDao: OcMessageDao,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _typing = MutableLiveData(false)
    val typing: LiveData<Boolean> = _typing

    private val _sending = MutableLiveData(false)
    val sending: LiveData<Boolean> = _sending

    var conversationId = ""
    var otherUserId = ""
    var myId = ""

    fun init(convId: String, otherId: String) {
        conversationId = convId
        otherUserId = otherId
        viewModelScope.launch { myId = chatRepo.getMyId() }
        socketManager.onNewMessage = { msg ->
            if (msg.conversationId == conversationId) {
                val current = _messages.value?.toMutableList() ?: mutableListOf()
                current.add(msg)
                _messages.postValue(current)
            }
        }
        socketManager.onTyping = { _, convId, isTyping ->
            if (convId == conversationId) _typing.postValue(isTyping)
        }
        socketManager.joinConversation(conversationId)
        loadMessages()
    }

    fun loadMessages() {
        viewModelScope.launch {
            // Show cached messages immediately
            val cached = messageDao.getByConversation(conversationId)
            if (cached.isNotEmpty()) {
                _messages.postValue(cached.map { e ->
                    com.meharenterprises.originconnect.data.model.Message(
                        id = e.id, conversationId = e.conversationId,
                        senderId = e.senderId, receiverId = e.receiverId,
                        type = e.type, content = e.content, mediaUrl = e.mediaUrl,
                        mediaThumbnail = e.mediaThumbnail, replyToId = e.replyToId,
                        status = e.status, isDeleted = e.isDeleted,
                        isDeletedForEveryone = e.isDeletedForEveryone,
                        isStarred = e.isStarred, createdAt = e.createdAt
                    )
                })
            }
            // Then fetch fresh from network (updates cache via ChatRepository)
            val fresh = chatRepo.getMessages(conversationId)
            if (fresh.isNotEmpty()) _messages.postValue(fresh)
            chatRepo.markRead(conversationId)
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        _sending.value = true
        viewModelScope.launch {
            val msg = chatRepo.sendMessage(otherUserId, content)
            if (msg != null) {
                val current = _messages.value?.toMutableList() ?: mutableListOf()
                current.add(msg)
                _messages.value = current
            }
            _sending.value = false
        }
    }

    fun sendTyping(isTyping: Boolean) {
        socketManager.sendTyping(conversationId, isTyping)
    }

    fun deleteMessage(messageId: String, forEveryone: Boolean) {
        viewModelScope.launch {
            chatRepo.deleteMessage(messageId, forEveryone)
            loadMessages()
        }
    }

    fun reactMessage(messageId: String, emoji: String) {
        viewModelScope.launch { chatRepo.reactMessage(messageId, emoji) }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.leaveConversation(conversationId)
    }
}
