package com.meharenterprises.originconnect.data.model
data class Contact(
    val contactId: String = "",
    val nickname: String? = null,
    val user: User = User()
)
