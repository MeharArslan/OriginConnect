package com.meharenterprises.originconnect.data.repository
import com.meharenterprises.originconnect.data.local.OcMessageDao
import com.meharenterprises.originconnect.data.local.OcMessageEntity
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.data.model.Message
import com.meharenterprises.originconnect.data.model.Message
import com.meharenterprises.originconnect.data.model.*
import com.meharenterprises.originconnect.data.remote.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager,
    private val messageDao: OcMessageDao
) {
    suspend fun getConversations(): List<Conversation> {
        val res = api.getConversations(session.getAuthHeader())
        return if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
    }

    suspend fun getMessages(conversationId: String, before: String? = null): List<Message> {
        return try {
            val res = api.getMessages(conversationId, session.getAuthHeader(), before)
            val list = if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
            if (list.isNotEmpty()) {
                messageDao.upsertAll(list.map { it.toEntity() })
            }
            if (list.isNotEmpty()) list else messageDao.getByConversation(conversationId).map { it.toMessage() }
        } catch (_: Exception) {
            messageDao.getByConversation(conversationId).map { it.toMessage() }
        }
    }

    suspend fun sendMessage(receiverId: String, content: String, type: String = "text", mediaUrl: String? = null, replyToId: String? = null): Message? {
        val res = api.sendMessage(SendMessageRequest(receiverId, type, content, mediaUrl, null, replyToId), session.getAuthHeader())
        return if (res.isSuccessful) res.body() else null
    }

    suspend fun markRead(conversationId: String) {
        try { api.markRead(conversationId, session.getAuthHeader()) } catch (_: Exception) {}
    }

    suspend fun deleteMessage(messageId: String, forEveryone: Boolean) {
        try { api.deleteMessage(messageId, forEveryone, session.getAuthHeader()) } catch (_: Exception) {}
    }

    suspend fun reactMessage(messageId: String, emoji: String) {
        try { api.reactMessage(messageId, ReactRequest(emoji), session.getAuthHeader()) } catch (_: Exception) {}
    }

    suspend fun starMessage(messageId: String, star: Boolean) {
        try { api.starMessage(messageId, mapOf("star" to star), session.getAuthHeader()) } catch (_: Exception) {}
    }

    suspend fun getContacts(): List<Contact> {
        val res = api.getContacts(session.getAuthHeader())
        return if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
    }

    suspend fun getUser(userId: String): User? {
        val res = api.getUser(userId, session.getAuthHeader())
        return if (res.isSuccessful) res.body() else null
    }

    suspend fun getMyId() = session.getUserId() ?: ""
}

private fun Message.toEntity() = OcMessageEntity(
    id = id, conversationId = conversationId, senderId = senderId,
    receiverId = receiverId, type = type, content = content,
    mediaUrl = mediaUrl, mediaThumbnail = mediaThumbnail,
    replyToId = replyToId, status = status, isDeleted = isDeleted,
    isDeletedForEveryone = isDeletedForEveryone, isStarred = isStarred,
    createdAt = createdAt
)

private fun OcMessageEntity.toMessage() = Message(
    id = id, conversationId = conversationId, senderId = senderId,
    receiverId = receiverId, type = type, content = content,
    mediaUrl = mediaUrl, mediaThumbnail = mediaThumbnail,
    replyToId = replyToId, status = status, isDeleted = isDeleted,
    isDeletedForEveryone = isDeletedForEveryone, isStarred = isStarred,
    createdAt = createdAt
)

private fun Message.toEntity() = OcMessageEntity(
    id = id, conversationId = conversationId, senderId = senderId,
    receiverId = receiverId, type = type, content = content,
    mediaUrl = mediaUrl, mediaThumbnail = mediaThumbnail,
    replyToId = replyToId, status = status, isDeleted = isDeleted,
    isDeletedForEveryone = isDeletedForEveryone, isStarred = isStarred,
    createdAt = createdAt
)

private fun OcMessageEntity.toMessage() = Message(
    id = id, conversationId = conversationId, senderId = senderId,
    receiverId = receiverId, type = type, content = content,
    mediaUrl = mediaUrl, mediaThumbnail = mediaThumbnail,
    replyToId = replyToId, status = status, isDeleted = isDeleted,
    isDeletedForEveryone = isDeletedForEveryone, isStarred = isStarred,
    createdAt = createdAt
)
