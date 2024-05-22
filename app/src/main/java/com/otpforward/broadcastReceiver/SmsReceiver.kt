package com.otpforward.broadcastReceiver

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.core.app.ActivityCompat

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            if (bundle != null) {
                try {
                    val pdus = bundle["pdus"] as Array<*>
                    val messages = pdus.map { pdu ->
                        SmsMessage.createFromPdu(pdu as ByteArray)
                    }

                    for (message in messages) {
                        val smsBody = message.messageBody
                        val otp = extractOTPFromMessage(smsBody)
                        if (otp != null) {
                            val sharedPreferences = context!!.getSharedPreferences("sms_preferences", Context.MODE_PRIVATE)
                            val destinationNumber = sharedPreferences.getString("destination_number", null)
                            val subscriptionId = sharedPreferences.getInt("subscription_id", -1)

                            if (destinationNumber != null && subscriptionId != -1) {
                                forwardMessage(context, smsBody, destinationNumber, subscriptionId)
                            }
                            abortBroadcast()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun extractOTPFromMessage(smsEvent: String): String? {
        val otpPattern = "\\b\\d{6}\\b".toRegex()
        val matchResult = otpPattern.find(smsEvent)
        return matchResult?.value
    }

    private fun forwardMessage(context: Context?, message: String, destinationNumber: String, subscriptionId: Int) {
        val sentIntent = PendingIntent.getBroadcast(context, 0, Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE)
        val deliveryIntent = PendingIntent.getBroadcast(context, 0, Intent("SMS_DELIVERED"), PendingIntent.FLAG_IMMUTABLE)

        val subscriptionManager = SubscriptionManager.from(context)
        val subscriptionInfoList = if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else subscriptionManager.activeSubscriptionInfoList

        val smsManager: SmsManager = if (subscriptionInfoList.any { it.subscriptionId == subscriptionId }) {
            SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
        } else {
            SmsManager.getDefault()
        }

        smsManager.sendTextMessage(destinationNumber, null, message, sentIntent, deliveryIntent)
    }
}
