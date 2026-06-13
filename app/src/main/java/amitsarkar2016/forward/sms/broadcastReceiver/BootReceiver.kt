package amitsarkar2016.forward.sms.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import amitsarkar2016.forward.sms.utils.ServiceUtils

/**
 * Restarts the foreground service after device reboot
 * so SMS forwarding continues without user intervention.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted — restarting foreground service")
            ServiceUtils.startForegroundServiceIfNeeded(context)
        }
    }
}
