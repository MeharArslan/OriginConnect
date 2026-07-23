package com.meharenterprises.originconnect.data.model
data class Conversation(
    val id: String = "",
    val otherUserId: String = "",
    val lastMessageContent: String? = null,
    val lastMessageAt: Long? = null,
    val unreadCount: Int = 0,
    val participant1Id: String = "",
    val participant2Id: String = ""
)
