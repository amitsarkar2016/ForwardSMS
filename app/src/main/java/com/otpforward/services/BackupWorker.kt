package com.otpforward.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.otpforward.ui.activity.MainActivity
import com.otpforward.utils.ServiceUtils

class BackupWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Start the foreground service before proceeding
//        ServiceUtils.startForegroundServiceIfNeeded(applicationContext)

//        // Notification setup (optional)
//        createNotificationChannel(applicationContext)
//        showNotification("Backup Started", "Your daily backup has started.")
//        ServiceUtils.startForegroundServiceIfNeeded(applicationContext)

//        Log.d("BackupWorker", "Backup started")
        try {
            // Simulate the backup process
//            Thread.sleep(3000)  // Replace with actual backup logic

//            Log.d("BackupWorker", "Backup completed successfully")
//            showNotification("Backup Completed", "Your daily backup has been completed.")
            ServiceUtils.startForegroundServiceIfNeeded(applicationContext)
            return Result.success()
        } catch (e: Exception) {
//            Log.e("BackupWorker", "Backup failed", e)
//            showNotification("Backup Failed", "An error occurred during the backup.")
            return Result.failure()
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationId = 1
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "BACKUP_CHANNEL")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel(context: Context) {
        // Check if the Android version is 8.0 (Oreo) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Define the importance level for the notification channel
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            // Create a NotificationChannel object
            val channel = NotificationChannel("BACKUP_CHANNEL", "Backup Notifications", importance).apply {
                description = "Channel for backup status notifications"
            }

            // Get the NotificationManager system service
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }
}
