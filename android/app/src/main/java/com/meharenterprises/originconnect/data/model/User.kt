package com.meharenterprises.originconnect.data.model
data class User(
    val id: String = "",
    val phone: String = "",
    val displayName: String = "",
    val about: String? = null,
    val photoUrl: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val lastSeenPrivacy: String = "everyone",
    val photoPrivacy: String = "everyone",
    val createdAt: String = ""
)
