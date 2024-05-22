package com.otpforward.broadcastReceiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class SmsDeliveredReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "SMS_DELIVERED") {
            val resultCode = resultCode
            val resultData = resultData
            // Handle the delivery status of the SMS message
            if (resultCode == Activity.RESULT_OK) {
                // SMS message was delivered successfully
                showToast(context, "SMS delivered successfully")
            } else {
                // SMS message delivery failed
                val failureReason = getFailureReason(resultCode)
                showToast(context, "Failed to deliver SMS: $failureReason")
            }
        }
    }

    private fun showToast(context: Context?, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    private fun getFailureReason(resultCode: Int): String {
        return when (resultCode) {
            Activity.RESULT_CANCELED -> "Canceled by the network"
            else -> "Unknown error"
        }
    }
}