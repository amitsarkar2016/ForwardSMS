package com.otpforward.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.otpforward.R

class MyForegroundService : Service() {

    companion object {
        const val MY_CHANNEL_ID = "MY_CHANNEL_ID"
        const val MY_CHANNEL_NAME = "OTP Forward"
        const val RESTART_SERVICE_ACTION = "com.otpforward.action.RESTART_SERVICE"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MY_CHANNEL_ID,
                MY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            val restartIntent = Intent(this, MyForegroundService::class.java).apply {
                action = RESTART_SERVICE_ACTION
            }
            val pendingRestartIntent = PendingIntent.getService(
                this, 0, restartIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = Notification.Builder(this, MY_CHANNEL_ID)
                .setContentTitle("Service Running")
                .setContentText("Processing SMS...")
                .setSmallIcon(R.drawable.ic_notification)
                .addAction(
                    Notification.Action.Builder(
                        null,
                        "Restart Service",
                        pendingRestartIntent
                    ).build()
                )
                .setOngoing(true)
                .build()

            startForeground(1, notification)
        }

        if (intent?.action == RESTART_SERVICE_ACTION) {
            // Handle the restart logic
            stopSelf()
            startService(Intent(this, MyForegroundService::class.java))
        }

        return START_STICKY
    }
}