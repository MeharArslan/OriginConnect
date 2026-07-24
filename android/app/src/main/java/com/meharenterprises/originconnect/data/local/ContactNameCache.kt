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
    private val userIdToPhoto= mutableMapOf<String, String?>()
    private var loaded = false

    fun loadDeviceContacts() {
        if (loaded) return
        loaded = true
        try {
            val cur = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
                null, null, null) ?: return
            cur.use {
                val numCol  = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                while (it.moveToNext()) {
                    val raw  = it.getString(numCol)?.trim() ?: continue
                    val name = it.getString(nameCol)?.trim()?.takeIf { n -> n.isNotEmpty() } ?: continue
                    storeVariants(raw, name)
                }
            }
        } catch (_: Exception) {}
    }

    fun forceReload() { loaded = false; loadDeviceContacts() }

    fun putPhone(phone: String, name: String) = storeVariants(phone, name)

    private fun storeVariants(raw: String, name: String) {
        val digits = raw.filter { it.isDigit() || it == '+' }
        listOf(raw, digits).forEach { phoneToName[it] = name }
        when {
            digits.startsWith("+92") && digits.length >= 12 -> {
                phoneToName["0" + digits.drop(3)] = name
                phoneToName[digits.drop(3)] = name
            }
            digits.startsWith("0") && digits.length >= 10 -> {
                phoneToName["+92" + digits.drop(1)] = name
                phoneToName[digits.drop(1)] = name
            }
            digits.length == 10 -> {
                phoneToName["+92$digits"] = name
                phoneToName["0$digits"] = name
            }
        }
    }

    fun resolvePhone(phone: String): String? {
        loadDeviceContacts()
        return phoneToName[phone] ?: phoneToName[phone.filter { it.isDigit() || it == '+' }]
    }

    fun putUserId(userId: String, phone: String, fallback: String, photoUrl: String? = null) {
        userIdToPhone[userId] = phone
        userIdToPhoto[userId] = photoUrl
        val localName = resolvePhone(phone)
        userIdToName[userId] = localName ?: fallback
    }

    fun resolveUserId(userId: String): String? {
        val phone = userIdToPhone[userId]
        if (phone != null) {
            val local = resolvePhone(phone)
            if (local != null) { userIdToName[userId] = local; return local }
        }
        return userIdToName[userId]
    }

    fun getPhotoUrl(userId: String): String? = userIdToPhoto[userId]
}
