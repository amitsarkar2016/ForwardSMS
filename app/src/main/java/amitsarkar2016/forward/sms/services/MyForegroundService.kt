package amitsarkar2016.forward.sms.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import amitsarkar2016.forward.sms.R
import amitsarkar2016.forward.sms.ui.activity.MainActivity

class MyForegroundService : Service() {

    companion object {
        const val MY_CHANNEL_ID = "MY_CHANNEL_ID"
        const val MY_CHANNEL_NAME = "OTP Forward"
        const val RESTART_SERVICE_ACTION = "amitsarkar2016.forward.sms.action.RESTART_SERVICE"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create Notification Channel
            val channel = NotificationChannel(
                MY_CHANNEL_ID,
                MY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            // Restart Service Intent
            val restartIntent = Intent(this, MyForegroundService::class.java).apply {
                action = RESTART_SERVICE_ACTION
            }
            val pendingRestartIntent = PendingIntent.getService(
                this, 0, restartIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Main Activity Intent
            val mainActivityIntent = Intent(applicationContext, MainActivity::class.java)
            mainActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE)

            // Build Notification
            val notification = NotificationCompat.Builder(this, MY_CHANNEL_ID)
                .setContentTitle("Service Running")
                .setContentText("Processing SMS...")
                .setSmallIcon(R.drawable.ic_notification)
                .addAction(
                    NotificationCompat.Action(
                        null,
                        "Restart Service",
                        pendingRestartIntent
                    )
                )
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()

            // Start Foreground Service
            startForeground(1, notification)
        }

        // Restart Service Logic
        if (intent?.action == RESTART_SERVICE_ACTION) {
            stopSelf()
            startService(Intent(this, MyForegroundService::class.java))
        }

        return START_STICKY
    }
}
