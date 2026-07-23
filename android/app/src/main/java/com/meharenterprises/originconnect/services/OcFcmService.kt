package com.meharenterprises.originconnect.services
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

class OcFcmService : FirebaseMessagingService() {
    override fun onMessageReceived(msg: RemoteMessage) {
        Log.d("FCM", "Message from: ${msg.from}")
        // Handle push notification — show local notification
    }
    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        // Send token to backend
    }
}
