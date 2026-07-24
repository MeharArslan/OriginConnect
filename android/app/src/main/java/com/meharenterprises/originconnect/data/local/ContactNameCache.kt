package com.meharenterprises.originconnect.data.local
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactNameCache @Inject constructor() {
    // phone → local contact name from device
    private val phoneToName = mutableMapOf<String, String>()
    // userId → local contact name (resolved via phone)
    private val userIdToName = mutableMapOf<String, String>()
    // userId → phone
    private val userIdToPhone = mutableMapOf<String, String>()

    fun putPhone(phone: String, name: String) {
        // Store all variants
        phoneToName[phone] = name
        val digits = phone.filter { it.isDigit() || it == '+' }
        phoneToName[digits] = name
        if (digits.startsWith("+92") && digits.length >= 12) {
            phoneToName["0" + digits.drop(3)] = name
            phoneToName[digits.drop(3)] = name
        } else if (digits.startsWith("0") && digits.length >= 10) {
            phoneToName["+92" + digits.drop(1)] = name
        }
    }

    fun resolvePhone(phone: String): String? = phoneToName[phone]
        ?: phoneToName[phone.filter { it.isDigit() || it == '+' }]

    fun putUserId(userId: String, phone: String, fallbackName: String) {
        userIdToPhone[userId] = phone
        val localName = resolvePhone(phone)
        userIdToName[userId] = localName ?: fallbackName
    }

    fun resolveUserId(userId: String): String? = userIdToName[userId]

    fun getPhoneForUser(userId: String) = userIdToPhone[userId]
}
