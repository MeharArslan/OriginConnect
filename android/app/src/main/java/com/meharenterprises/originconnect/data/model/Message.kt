package com.meharenterprises.originconnect.data.model
data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val type: String = "text",
    val content: String? = null,
    val mediaUrl: String? = null,
    val mediaThumbnail: String? = null,
    val replyToId: String? = null,
    val reactions: Map<String, List<String>>? = null,
    val status: String = "sending",
    val isDeleted: Boolean = false,
    val isDeletedForEveryone: Boolean = false,
    val isEdited: Boolean = false,
    val isStarred: Boolean = false,
    val createdAt: String = "",
    val deliveredAt: Long? = null,
    val readAt: Long? = null
)
