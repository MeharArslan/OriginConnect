package com.meharenterprises.originconnect.data.repository
import com.meharenterprises.originconnect.data.local.SessionManager
import com.meharenterprises.originconnect.data.remote.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager
) {
    suspend fun sendOtp(phone: String) = api.sendOtp(SendOtpRequest(phone))

    suspend fun verifyOtp(phone: String, code: String): Result<String> {
        return try {
            val res = api.verifyOtp(VerifyOtpRequest(phone, code))
            if (res.isSuccessful && res.body() != null) {
                val body = res.body()!!
                session.saveSession(body.accessToken, body.refreshToken, body.user.id, body.user.phone, body.user.displayName, body.user.photoUrl)
                Result.success(if (body.isNewUser) "new" else "existing")
            } else {
                Result.failure(Exception(res.errorBody()?.string() ?: "Verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try { api.logout(session.getAuthHeader()) } catch (_: Exception) {}
        session.clearSession()
    }

    suspend fun isLoggedIn() = session.isLoggedIn()
}
