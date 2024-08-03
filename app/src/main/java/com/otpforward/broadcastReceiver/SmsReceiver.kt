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
import android.util.Log
import androidx.core.app.ActivityCompat
import com.otpforward.data.repository.UserSettingsRepository
import com.otpforward.ui.fragment.SettingType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras
        try {
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<*>?
                if (pdus != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val repository = UserSettingsRepository.getInstance(context)
                        repository.getUserSettings().collect { userSettings ->
                            if (userSettings != null) {
                                for (pdu in pdus) {
                                    val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                                    val messageBody = smsMessage.messageBody
                                    val sender = smsMessage.originatingAddress
                                    Log.d(
                                        "SmsReceiver", "Message received: $messageBody from $sender"
                                    )

                                    for (setting in userSettings) {
                                        when (setting.type) {
                                            SettingType.MATCH_CONTAIN -> {
                                                if (setting.data?.let {
                                                        matchContain(
                                                            messageBody, it
                                                        )
                                                    } != null) {
                                                    forwardMessage(
                                                        context,
                                                        messageBody,
                                                        setting.sendTo,
                                                        setting.subscriptionId.toInt()
                                                    )
                                                }
                                            }

                                            SettingType.ALL_SMS -> {
                                                forwardMessage(
                                                    context,
                                                    messageBody,
                                                    setting.sendTo,
                                                    setting.subscriptionId.toInt()
                                                )
                                            }

                                            SettingType.CARD_OTP -> {
                                                if (isCardOtp(messageBody)) {
                                                    forwardMessage(
                                                        context,
                                                        messageBody,
                                                        setting.sendTo,
                                                        setting.subscriptionId.toInt()
                                                    )
                                                }
                                            }

                                            SettingType.ALL_OTP -> {
                                                if (isMatchOtp(messageBody)) {
                                                    forwardMessage(
                                                        context,
                                                        messageBody,
                                                        setting.sendTo,
                                                        setting.subscriptionId.toInt()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Exception in onReceive", e)
        }
    }

    private fun isMatchOtp(message: String): Boolean {
        // Regular expression to match 4 to 6 digit OTPs preceded by "OTP is", "is OTP", or similar phrases
        val otpRegex = Regex(
            "(?:OTP\\s*(?:is|:|\\-|=)\\s*|\\b(?:is\\s+)?)(\\d{4,6})\\b", RegexOption.IGNORE_CASE
        )

        // Check if the message contains the pattern
        return otpRegex.containsMatchIn(message)
    }

    private fun isCardOtp(message: String): Boolean {
        // Regular expression to match 4 to 6 digit OTPs preceded by phrases commonly associated with card transactions
        val otpRegex = Regex(
            "(?:card\\s*otp|credit\\s*card\\s*otp|bank\\s*otp|otp\\s*(?:is|:|\\-|=)\\s*)\\d{4,6}\\b",
            RegexOption.IGNORE_CASE
        )

        // Check if the message contains the pattern
        return otpRegex.containsMatchIn(message)
    }

    private fun matchContain(message: String, data: String): String? {
        val regex = Regex(data)
        val matchResult = regex.find(message)
        return matchResult?.value
    }

    private fun forwardMessage(
        context: Context?, message: String, destinationNumber: String, subscriptionId: Int
    ) {
        val sentIntent =
            PendingIntent.getBroadcast(context, 0, Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE)
        val deliveryIntent = PendingIntent.getBroadcast(
            context, 0, Intent("SMS_DELIVERED"), PendingIntent.FLAG_IMMUTABLE
        )

        val subscriptionManager = SubscriptionManager.from(context)
        val subscriptionInfoList = if (ActivityCompat.checkSelfPermission(
                context!!, Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else subscriptionManager.activeSubscriptionInfoList

        val smsManager: SmsManager =
            if (subscriptionInfoList.any { it.subscriptionId == subscriptionId }) {
                SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
            } else {
                SmsManager.getDefault()
            }

        smsManager.sendTextMessage(destinationNumber, null, message, sentIntent, deliveryIntent)
    }
}