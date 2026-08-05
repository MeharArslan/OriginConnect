package com.meharenterprises.originconnect.data.remote

import com.meharenterprises.originconnect.data.local.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val session: SessionManager,
    private val apiService: dagger.Lazy<ApiService>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val response = chain.proceed(original)

        // Token expired — attempt silent refresh once
        if (response.code == 401) {
            response.close()
            val newToken = runBlocking { tryRefresh() } ?: return chain.proceed(original)
            val retried = original.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
            return chain.proceed(retried)
        }
        return response
    }

    private suspend fun tryRefresh(): String? {
        return try {
            val refresh = session.getRefreshToken() ?: return null
            val res = apiService.get().refreshToken(
                RefreshRequest(refresh),
                "Bearer ${session.getAccessToken() ?: ""}"
            )
            if (res.isSuccessful) {
                val body = res.body() ?: return null
                session.saveSession(
                    body.accessToken, body.refreshToken,
                    body.user.id, body.user.phone,
                    body.user.displayName, body.user.photoUrl
                )
                body.accessToken
            } else null
        } catch (_: Exception) { null }
    }
}
