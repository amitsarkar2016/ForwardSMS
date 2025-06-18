package amitsarkar2016.forward.sms.ui.activity

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import amitsarkar2016.forward.sms.R
import amitsarkar2016.forward.sms.services.MyForegroundService
import amitsarkar2016.forward.sms.ui.extention.replaceFragment
import amitsarkar2016.forward.sms.ui.fragment.HomeFragment
import amitsarkar2016.forward.sms.utils.ServiceUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ServiceUtils.startForegroundServiceIfNeeded(this)

        // Call scheduleDailyBackup when the app starts
        ServiceUtils.scheduleDailyBackup(this)
//        scheduleAlarmEveryMinute(this)

        val fragment = HomeFragment()
        replaceFragment(fragment, R.id.mainContainer, false)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarmEveryMinute(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MyForegroundService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // For API 19 and above, use setExact to ensure it triggers every minute precisely
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 60 * 1000, // Trigger 1 minute later
            pendingIntent
        )
    }


}
