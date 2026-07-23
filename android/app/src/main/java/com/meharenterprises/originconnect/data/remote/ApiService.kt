package com.meharenterprises.originconnect.data.remote
import com.meharenterprises.originconnect.data.model.*
import retrofit2.Response
import retrofit2.http.*

data class SendOtpRequest(val phone: String)
data class VerifyOtpRequest(val phone: String, val code: String)
data class RefreshRequest(val refreshToken: String)
data class SendOtpResponse(val message: String, val code: String?)
data class AuthResponse(val accessToken: String, val refreshToken: String, val isNewUser: Boolean, val user: User)
data class SendMessageRequest(val receiverId: String, val type: String, val content: String? = null, val mediaUrl: String? = null, val mediaThumbnail: String? = null, val replyToId: String? = null)
data class SyncContactsRequest(val phones: List<String>)
data class UpdateProfileRequest(val displayName: String? = null, val about: String? = null)
data class ReactRequest(val emoji: String)

interface ApiService {
    @POST("auth/send-otp") suspend fun sendOtp(@Body req: SendOtpRequest): Response<SendOtpResponse>
    @POST("auth/verify-otp") suspend fun verifyOtp(@Body req: VerifyOtpRequest): Response<AuthResponse>
    @POST("auth/refresh") suspend fun refreshToken(@Body req: RefreshRequest, @Header("Authorization") token: String): Response<AuthResponse>
    @POST("auth/logout") suspend fun logout(@Header("Authorization") token: String): Response<Any>
    @GET("auth/me") suspend fun getMe(@Header("Authorization") token: String): Response<User>
    @PUT("users/profile") suspend fun updateProfile(@Body req: UpdateProfileRequest, @Header("Authorization") token: String): Response<User>
    @GET("users/{id}") suspend fun getUser(@Path("id") id: String, @Header("Authorization") token: String): Response<User>
    @POST("contacts/sync") suspend fun syncContacts(@Body req: SyncContactsRequest, @Header("Authorization") token: String): Response<Any>
    @GET("contacts") suspend fun getContacts(@Header("Authorization") token: String): Response<List<Contact>>
    @GET("conversations") suspend fun getConversations(@Header("Authorization") token: String): Response<List<Conversation>>
    @POST("messages") suspend fun sendMessage(@Body req: SendMessageRequest, @Header("Authorization") token: String): Response<Message>
    @GET("conversations/{id}/messages") suspend fun getMessages(@Path("id") id: String, @Header("Authorization") token: String, @Query("before") before: String? = null): Response<List<Message>>
    @POST("conversations/{id}/read") suspend fun markRead(@Path("id") id: String, @Header("Authorization") token: String): Response<Any>
    @DELETE("messages/{id}") suspend fun deleteMessage(@Path("id") id: String, @Query("forEveryone") forEveryone: Boolean, @Header("Authorization") token: String): Response<Any>
    @POST("messages/{id}/react") suspend fun reactMessage(@Path("id") id: String, @Body req: ReactRequest, @Header("Authorization") token: String): Response<Any>
    @POST("messages/{id}/star") suspend fun starMessage(@Path("id") id: String, @Body body: Map<String, Boolean>, @Header("Authorization") token: String): Response<Any>
    @Multipart @POST("media/upload") suspend fun uploadMedia(@Part file: okhttp3.MultipartBody.Part, @Header("Authorization") token: String): Response<Any>
}
