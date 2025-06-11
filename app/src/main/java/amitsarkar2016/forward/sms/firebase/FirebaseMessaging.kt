package amitsarkar2016.forward.sms.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import amitsarkar2016.forward.sms.firebase.FirebaseFunction.displayNotification
import amitsarkar2016.forward.sms.firebase.FirebaseFunction.sendTokenToServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseMessaging : FirebaseMessagingService() {
    companion object {
        const val TAG = "FirebaseMessaging"
        const val CHANNEL_ID = "default_channel"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Handle token refresh here
        CoroutineScope(Dispatchers.IO).launch{
            sendTokenToServer(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if the message contains a notification payload
        remoteMessage.notification?.let {
            // Extract notification details
            val title = it.title
            val body = it.body

            // Display notification
            displayNotification(title, body, applicationContext)
        }
    }


}
