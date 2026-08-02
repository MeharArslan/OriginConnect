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
        try {
            val cur = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
                null, null, null) ?: return  // permission denied — do NOT set loaded=true
            var count = 0
            cur.use {
                val numCol  = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                while (it.moveToNext()) {
                    val raw  = it.getString(numCol)?.trim() ?: continue
                    val name = it.getString(nameCol)?.trim()?.takeIf { n -> n.isNotEmpty() } ?: continue
                    storeVariants(raw, name)
                    count++
                }
            }
            if (count > 0) loaded = true  // only mark loaded if we actually got contacts
        } catch (_: Exception) {}
    }

    fun forceReload() { loaded = false; loadDeviceContacts() }

    /** Called on every load() — reloads if process was killed and restarted */
    fun ensureLoaded() {
        if (!loaded) loadDeviceContacts()
    }

    fun preloadFromRoom(entities: List<OcContactEntity>) {
        entities.forEach { e ->
            userIdToPhone[e.userId] = e.phone
            userIdToPhoto[e.userId] = e.photoUrl
            val best = e.localName?.takeIf { it.isNotEmpty() } ?: e.serverName
            userIdToName[e.userId] = best
            if (best.isNotEmpty()) storeVariants(e.phone, best)
        }
    }

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
        // Always try device name first; fall back to server name
        val localName = resolvePhone(phone)
        userIdToName[userId] = if (!localName.isNullOrEmpty()) localName else fallback
    }

    fun resolveUserId(userId: String): String? {
        val phone = userIdToPhone[userId]
        if (phone != null) {
            // Re-check device contacts every time — they may have loaded after putUserId was called
            ensureLoaded()
            val local = resolvePhone(phone)
            if (!local.isNullOrEmpty()) {
                userIdToName[userId] = local
                return local
            }
        }
        // Return cached name (could be server name if device name not found)
        return userIdToName[userId]?.takeIf { it.isNotEmpty() }
    }

    fun getPhotoUrl(userId: String): String? = userIdToPhoto[userId]
}
