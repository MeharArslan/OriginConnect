package com.meharenterprises.originconnect.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oc_conversations")
data class OcConversationEntity(
    @PrimaryKey val id: String,
    val otherUserId: String,
    val lastMessageContent: String?,
    val lastMessageAt: Long?,
    val unreadCount: Int,
    val participant1Id: String,
    val participant2Id: String
)
