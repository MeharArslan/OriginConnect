package com.meharenterprises.originconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oc_messages")
data class OcMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val type: String,
    val content: String?,
    val mediaUrl: String?,
    val mediaThumbnail: String?,
    val replyToId: String?,
    val status: String,
    val isDeleted: Boolean,
    val isDeletedForEveryone: Boolean,
    val isStarred: Boolean,
    val createdAt: String
)
