package com.otpforward.broadcastReceiver

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.regex.Pattern

/**
 * @Author: Amit Sarkar
 * @Date: 24-01-2024
 */
class SmsReceiver : BroadcastReceiver() {

    private lateinit var permissionCallback: (Boolean) -> Unit
    fun setPermissionCallback(callback: (Boolean) -> Unit) {
        this.permissionCallback = callback
    }

    private lateinit var otpCallback: (String) -> Unit

    fun getOtpCallback(callback: (String) -> Unit) {
        this.otpCallback = callback
    }

    override fun onReceive(context: Context?, intent: Intent?) {
//        if (ContextCompat.checkSelfPermission(
//                context!!,
//                Manifest.permission.RECEIVE_SMS
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            // Permission already granted, proceed with SMS handling
//            handleSms(intent)
//        } else {
//            permissionCallback.invoke(false)
//        }

        if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<*>
                for (pdu in pdus) {
                    val message = SmsMessage.createFromPdu(pdu as ByteArray)
                    val smsBody = message.messageBody
                    // Extract OTP from the SMS body and handle it
                    val otp = extractOTPFromMessage(smsBody)
                    if (otp != null) {
                        Toast.makeText(context, otp, Toast.LENGTH_SHORT).show()
                        // Prevent other BroadcastReceivers from processing this SMS
                        abortBroadcast()
                        // You may also unregister the receiver if needed
                        // context.unregisterReceiver(this)
                    }
                }
            }
        }
    }

    private fun extractOTPFromMessage(smsEvent: String): String? {
        // Define a regular expression pattern for a 6-digit OTP
        val otpPattern = "\\b\\d{6}\\b".toRegex()

        // Search for the OTP pattern in the SMS message
        val matchResult = otpPattern.find(smsEvent)

        // Extract and return the OTP if found
        return matchResult?.value
    }

    private fun forwardMessage(message: String, destinationNumber: String, context: Context) {
        val sentIntent = PendingIntent.getBroadcast(context, 0, Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE)
        val deliveryIntent = PendingIntent.getBroadcast(context, 0, Intent("SMS_DELIVERED"), PendingIntent.FLAG_IMMUTABLE)

        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            destinationNumber,
            null,
            message,
            sentIntent,
            deliveryIntent
        )
    }

    private fun handleSms(intent: Intent?) {
        val bundle = intent?.extras
        if (bundle != null) {
            val pdus = bundle.get("pdus") as Array<*>
            val messages = arrayOfNulls<SmsMessage>(pdus.size)
            for (i in pdus.indices) {
                messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                val messageBody = messages[i]?.messageBody ?: ""
                extractOtp(messageBody)
            }
        }
    }

    private fun extractOtp(messageBody: String) {
        val otpPattern = Pattern.compile("\\b\\d{4}\\b")
        val matcher = otpPattern.matcher(messageBody)
        if (matcher.find()) {
            val otp = matcher.group()
            // Do something with the OTP (e.g., fill an OTP field)
            otpCallback.invoke(otp)
        }
    }

}
