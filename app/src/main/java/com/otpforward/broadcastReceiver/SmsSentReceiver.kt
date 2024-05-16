package com.otpforward.broadcastReceiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.widget.Toast

/**
 * @Author: Amit Sarkar
 * @Date: 16-05-2024
 */
class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "SMS_SENT") {
            val resultCode = resultCode
            val resultData = resultData
            // Handle the sent status of the SMS message
            if (resultCode == Activity.RESULT_OK) {
                // SMS message was successfully sent
                // You can implement further logic here if needed
                showToast(context, "SMS sent successfully")
            } else {
                // SMS message sending failed
                // You can implement error handling or retry mechanism here
                val failureReason = getFailureReason(resultCode)
                showToast(context, "Failed to send SMS: $failureReason")
            }
        }
    }

    private fun showToast(context: Context?, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun getFailureReason(resultCode: Int): String {
        return when (resultCode) {
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Generic failure"
            SmsManager.RESULT_ERROR_NO_SERVICE -> "No service"
            SmsManager.RESULT_ERROR_NULL_PDU -> "Null PDU"
            SmsManager.RESULT_ERROR_RADIO_OFF -> "Radio off"
            else -> "Unknown error"
        }
    }
}

