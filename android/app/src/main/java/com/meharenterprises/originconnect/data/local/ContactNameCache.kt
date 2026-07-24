package com.meharenterprises.originconnect.data.local
import android.content.Context
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactNameCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val phoneToName  = mutableMapOf<String, String>()
    private val userIdToName = mutableMapOf<String, String>()
    private val userIdToPhone= mutableMapOf<String, String>()
    private var loaded = false

    /** Call once on startup or when contacts change */
    fun loadDeviceContacts() {
        if (loaded) return
        loaded = true
        try {
            val cur = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                ), null, null, null
            ) ?: return
            cur.use {
                val numCol  = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                while (it.moveToNext()) {
                    val raw  = it.getString(numCol)?.trim() ?: continue
                    val name = it.getString(nameCol)?.trim().takeIf { n -> n?.isNotEmpty() == true } ?: continue
                    storeAllVariants(raw, name)
                }
            }
        } catch (_: Exception) {}
    }

    fun forceReload() { loaded = false; loadDeviceContacts() }

    private fun storeAllVariants(raw: String, name: String) {
        val digits = raw.filter { it.isDigit() || it == '+' }
        phoneToName[raw]    = name
        phoneToName[digits] = name
        when {
            digits.startsWith("+92") && digits.length >= 12 -> {
                phoneToName["0" + digits.drop(3)] = name   // +923001234567 → 03001234567
                phoneToName[digits.drop(3)] = name          // +923001234567 → 3001234567
            }
            digits.startsWith("0") && digits.length >= 10 -> {
                phoneToName["+92" + digits.drop(1)] = name  // 03001234567 → +923001234567
                phoneToName[digits.drop(1)] = name
            }
            digits.length == 10 -> {
                phoneToName["+92$digits"] = name
                phoneToName["0$digits"] = name
            }
        }
    }

    fun resolve(phone: String): String? {
        loadDeviceContacts()
        return phoneToName[phone]
            ?: phoneToName[phone.filter { it.isDigit() || it == '+' }]
    }

    fun putUserId(userId: String, phone: String, fallback: String) {
        userIdToPhone[userId] = phone
        userIdToName[userId]  = resolve(phone) ?: fallback
    }

    fun resolveUserId(userId: String): String? {
        // Re-check device contacts in case they updated
        val phone = userIdToPhone[userId]
        if (phone != null) {
            val localName = resolve(phone)
            if (localName != null) {
                userIdToName[userId] = localName
                return localName
            }
        }
        return userIdToName[userId]
    }

    fun resolvePhone(phone: String) = resolve(phone)
}
