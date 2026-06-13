package amitsarkar2016.forward.sms.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import amitsarkar2016.forward.sms.firebase.FirebaseFunction.displayNotification

class FirebaseMessaging : FirebaseMessagingService() {
    companion object {
        const val TAG = "FirebaseMessaging"
        const val CHANNEL_ID = "default_channel"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // TODO: Send token to your backend via the app's ApiService when ready
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            val title = it.title
            val body = it.body
            displayNotification(title, body, applicationContext)
        }
    }
}
