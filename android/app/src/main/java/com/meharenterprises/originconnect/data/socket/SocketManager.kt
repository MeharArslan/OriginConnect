package com.meharenterprises.originconnect.data.socket
import android.util.Log
import com.google.gson.Gson
import com.meharenterprises.originconnect.BuildConfig
import com.meharenterprises.originconnect.data.model.Message
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor() {

    private var socket: Socket? = null
    private val gson = Gson()
    private val TAG = "SocketManager"

    var onNewMessage: ((Message) -> Unit)? = null
    var onTyping: ((String, String, Boolean) -> Unit)? = null // userId, convId, isTyping
    var onPresence: ((String, Boolean, Long?) -> Unit)? = null // userId, isOnline, lastSeen
    var onMessageStatus: ((String, String) -> Unit)? = null // messageId, status

    fun connect(token: String) {
        try {
            val opts = IO.Options.builder()
                .setPath("/connect/socket.io")
                .setAuth(mapOf("token" to token))
                .build()
            socket = IO.socket(BuildConfig.SOCKET_URL, opts)
            socket?.apply {
                on(Socket.EVENT_CONNECT) { Log.d(TAG, "Socket connected") }
                on(Socket.EVENT_DISCONNECT) { Log.d(TAG, "Socket disconnected") }
                on("new_message") { args ->
                    try {
                        val msg = gson.fromJson(args[0].toString(), Message::class.java)
                        onNewMessage?.invoke(msg)
                    } catch (e: Exception) { Log.e(TAG, "Message parse error", e) }
                }
                on("typing") { args ->
                    try {
                        val obj = args[0] as JSONObject
                        onTyping?.invoke(obj.getString("userId"), obj.getString("conversationId"), obj.getBoolean("isTyping"))
                    } catch (e: Exception) {}
                }
                on("message_status") { args ->
                    try {
                        val obj = args[0] as JSONObject
                        onMessageStatus?.invoke(obj.getString("messageId"), obj.getString("status"))
                    } catch (e: Exception) {}
                }
                connect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Socket error: ${e.message}")
        }
    }

    fun joinConversation(conversationId: String) {
        socket?.emit("join_conversation", JSONObject().put("conversationId", conversationId))
    }

    fun leaveConversation(conversationId: String) {
        socket?.emit("leave_conversation", JSONObject().put("conversationId", conversationId))
    }

    fun sendTyping(conversationId: String, isTyping: Boolean) {
        socket?.emit("typing", JSONObject().put("conversationId", conversationId).put("isTyping", isTyping))
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }

    fun isConnected() = socket?.connected() == true
}
