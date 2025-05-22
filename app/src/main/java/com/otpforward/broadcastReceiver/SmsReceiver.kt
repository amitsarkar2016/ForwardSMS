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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale

class SmsReceiver : BroadcastReceiver() {

    private val otpPhrases = listOf(
        "otp", "your otp", "one-time password", "verification code", "enter this code",
        "security code", "authentication code", "login code", "access code", "confirmation code",
        "password reset code", "secret code", "authorization code", "account recovery code",
        "transaction code", "verify with this code", "use the code", "code for", "your pin is",
        "temporary code", "input the code", "code:", "is the otp for", "is secret otp for",
        "is your otp for", "otp for", "this otp is", "your code is"
    )

    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras
        try {
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<*>?
                if (pdus != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val repository = UserSettingsRepository.getInstance(context)
                        repository.getUserSettings().collect { userSettings ->
                            userSettings?.let { settings ->
                                for (pdu in pdus) {
                                    val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                                    val messageBody = smsMessage.messageBody
                                    val sender = smsMessage.originatingAddress
                                    Log.d("SmsReceiver", "Message received: $messageBody from $sender")

                                    for (setting in settings) {
                                        if (shouldForwardMessage(setting.type, messageBody, setting.data)) {
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
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Exception in onReceive", e)
        }
    }

    private fun shouldForwardMessage(
        settingType: SettingType,
        messageBody: String,
        settingData: String?
    ): Boolean {
        return when (settingType) {
            SettingType.MATCH_CONTAIN -> settingData?.let { matchContain(messageBody, it) } != null
            SettingType.ALL_SMS -> true
            SettingType.CARD_OTP -> settingData?.let {
                if (it.equals("all", ignoreCase = true) || it.equals("all otp", ignoreCase = true)) {
                    isCardOtp(messageBody, "") // Forward all card OTPs
                } else {
                    isCardOtp(messageBody, it)
                }
            } ?: false
            SettingType.ALL_OTP -> isMatchOtp(messageBody)
        }
    }

    private fun isMatchOtp(message: String): Boolean {
        val otpRegex = Regex("\\b\\d{4,6}\\b", RegexOption.IGNORE_CASE)
        val containsOtp = otpRegex.containsMatchIn(message)
        val containsPhrase = containsOtpPhrase(message)


        println("Contains OTP: $containsOtp")
        println("Contains OTP Phrase: $containsPhrase")
        return containsOtp && containsPhrase
    }

    private fun isCardOtp(message: String, cardNumber: String): Boolean {
        val otpRegex = Regex("\\b\\d{4,6}\\b", RegexOption.IGNORE_CASE)
        val containsCardNumber = cardNumber.isEmpty() || message.contains(cardNumber, ignoreCase = true)
        val containsOtp = otpRegex.containsMatchIn(message)
        val containsPhrase = containsOtpPhrase(message)

        return containsCardNumber && containsOtp && containsPhrase
    }

    private fun containsOtpPhrase(message: String): Boolean {
        val lowerCaseMessage = message.lowercase(Locale.getDefault())
        return otpPhrases.any { phrase -> phrase in lowerCaseMessage }
    }

    private fun matchContain(message: String, data: String): String? {
        val regex = Regex(data, RegexOption.IGNORE_CASE)
        return regex.find(message)?.value
    }

    private fun forwardMessage(
        context: Context?, message: String, destinationNumber: String, subscriptionId: Int
    ) {
        context?.let {
            if (ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("SmsReceiver", "READ_PHONE_STATE permission not granted")
                return
            }

            val sentIntent =
                PendingIntent.getBroadcast(it, 0, Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE)
            val deliveryIntent = PendingIntent.getBroadcast(
                it,
                0,
                Intent("SMS_DELIVERED"),
                PendingIntent.FLAG_IMMUTABLE
            )

            val subscriptionManager = SubscriptionManager.from(it)
            val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList ?: listOf()

            val smsManager: SmsManager = if (subscriptionInfoList.any { info -> info.subscriptionId == subscriptionId }) {
                    SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                } else {
                    SmsManager.getDefault()
                }

            Log.d("SmsReceiver", "Forwarding message to $destinationNumber using subscription $subscriptionId")
            smsManager.sendTextMessage(destinationNumber, null, message, sentIntent, deliveryIntent)
        } ?: run {
            Log.e("SmsReceiver", "Context is null")
        }
    }
}
