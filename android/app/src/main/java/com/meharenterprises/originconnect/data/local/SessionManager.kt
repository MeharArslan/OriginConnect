package com.meharenterprises.originconnect.data.local
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("oc_session")

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val ctx: Context) {

    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val USER_ID = stringPreferencesKey("user_id")
    private val USER_PHONE = stringPreferencesKey("user_phone")
    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_PHOTO = stringPreferencesKey("user_photo")

    suspend fun saveSession(accessToken: String, refreshToken: String, userId: String, phone: String, name: String, photo: String?) {
        ctx.dataStore.edit {
            it[ACCESS_TOKEN] = accessToken
            it[REFRESH_TOKEN] = refreshToken
            it[USER_ID] = userId
            it[USER_PHONE] = phone
            it[USER_NAME] = name
            if (photo != null) it[USER_PHOTO] = photo
        }
    }

    suspend fun getAccessToken(): String? = ctx.dataStore.data.map { it[ACCESS_TOKEN] }.first()
    suspend fun getRefreshToken(): String? = ctx.dataStore.data.map { it[REFRESH_TOKEN] }.first()
    suspend fun getUserId(): String? = ctx.dataStore.data.map { it[USER_ID] }.first()
    suspend fun getUserPhone(): String? = ctx.dataStore.data.map { it[USER_PHONE] }.first()
    suspend fun getUserName(): String? = ctx.dataStore.data.map { it[USER_NAME] }.first()
    suspend fun getUserPhoto(): String? = ctx.dataStore.data.map { it[USER_PHOTO] }.first()
    suspend fun isLoggedIn(): Boolean = !getAccessToken().isNullOrEmpty()

    suspend fun getAuthHeader(): String = "Bearer ${getAccessToken() ?: ""}"

    suspend fun clearSession() {
        ctx.dataStore.edit { it.clear() }
    }
}
