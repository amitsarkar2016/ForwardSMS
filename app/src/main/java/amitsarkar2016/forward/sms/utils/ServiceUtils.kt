package amitsarkar2016.forward.sms.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import amitsarkar2016.forward.sms.services.BackupWorker
import amitsarkar2016.forward.sms.services.MyForegroundService
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ServiceUtils {

    fun startForegroundServiceIfNeeded(context: Context) {
        val serviceIntent = Intent(context, MyForegroundService::class.java).apply {
            action = "amitsarkar2016.forward.sms.action.MY_SERVICE_ACTION"
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                "amitsarkar2016.forward.sms.permission.MY_SERVICE_PERMISSION"
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    fun scheduleDailyBackup(context: Context) {
        val backupHour9 = 9   // 9 AM
        val backupHour23 = 23 // 11 PM
        val backupMinute = 50

        val currentTime = Calendar.getInstance()

        // Set the target time for the backup (9:50 AM today)
        val targetTime9 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, backupHour9)
            set(Calendar.MINUTE, backupMinute)
            set(Calendar.SECOND, 0)
        }
        // Set the target time for the backup (11:50 PM today)
        val targetTime23 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, backupHour23)
            set(Calendar.MINUTE, backupMinute)
            set(Calendar.SECOND, 0)
        }

        // If the target time has already passed today, schedule it for tomorrow
        if (targetTime9.before(currentTime)) {
            targetTime9.add(Calendar.DAY_OF_MONTH, 1)
        }
        if (targetTime23.before(currentTime)) {
            targetTime23.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Calculate the initial delay (in minutes) before the first backup
        // BUG FIX: was using targetTime23 for both — now correctly uses targetTime9
        val timeDifference9 = targetTime9.timeInMillis - currentTime.timeInMillis
        val initialDelay9 = TimeUnit.MILLISECONDS.toMinutes(timeDifference9)

        val timeDifference23 = targetTime23.timeInMillis - currentTime.timeInMillis
        val initialDelay23 = TimeUnit.MILLISECONDS.toMinutes(timeDifference23)

        // Create periodic work requests with a 24-hour repeat interval
        val backupWorkRequest9 = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay9, TimeUnit.MINUTES)
            .build()

        val backupWorkRequest23 = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay23, TimeUnit.MINUTES)
            .build()

        // BUG FIX: Use different unique names so the second doesn't replace the first
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailyBackup_9AM",
            ExistingPeriodicWorkPolicy.KEEP,
            backupWorkRequest9
        )
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailyBackup_11PM",
            ExistingPeriodicWorkPolicy.KEEP,
            backupWorkRequest23
        )
    }
}
