package amitsarkar2016.forward.sms.firebase

import amitsarkar2016.forward.sms.R
import amitsarkar2016.forward.sms.ui.activity.MainActivity
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import gowebs.matka.club.api.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FirebaseFunction {
    fun displayNotification(title: String?, body: String?, context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, FirebaseMessaging.Companion.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        // Create a notification manager
        val notificationManager = NotificationManagerCompat.from(context)

        // Notification channels are required for Android O (API 26) and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(FirebaseMessaging.Companion.CHANNEL_ID, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        notificationManager.notify(0, builder.build())
    }

    suspend fun sendTokenToServer(token: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.29.17/firebase_notification/")
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()


        val apiService = retrofit.create(ApiService::class.java)
//        apiService.sendToken(token)
        apiService.sendTokenService(token)
    }
}