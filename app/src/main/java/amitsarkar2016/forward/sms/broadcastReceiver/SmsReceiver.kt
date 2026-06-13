package amitsarkar2016.forward.sms.broadcastReceiver

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.app.ActivityCompat
import amitsarkar2016.forward.sms.data.model.SettingType
import amitsarkar2016.forward.sms.data.repository.UserSettingsRepository
import amitsarkar2016.forward.sms.di.SmsReceiverEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
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
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val bundle = intent.extras ?: return@launch
                val pdus = bundle["pdus"] as? Array<*> ?: return@launch
                val format = bundle.getString("format")

                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    SmsReceiverEntryPoint::class.java
                )
                val settings = entryPoint.userSettingsRepository()
                    .getUserSettings()
                    .first()
                    .filter { it.isActive }

                for (pdu in pdus) {
                    val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray, format)
                    val messageBody = smsMessage.messageBody ?: continue
                    val sender = smsMessage.originatingAddress
                    Log.d("SmsReceiver", "Message received from $sender")

                    for (setting in settings) {
                        if (shouldForwardMessage(setting.type, messageBody, setting.data)) {
                            forwardMessage(
                                context,
                                messageBody,
                                setting.sendTo,
                                setting.subscriptionId.toIntOrNull() ?: -1
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error processing SMS", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun shouldForwardMessage(
        settingType: SettingType,
        messageBody: String,
        settingData: String?
    ): Boolean {
        return when (settingType) {
            SettingType.MATCH_CONTAIN -> !settingData.isNullOrEmpty() &&
                    messageBody.contains(settingData, ignoreCase = true)
            SettingType.ALL_SMS -> true
            SettingType.CARD_OTP -> settingData?.let {
                if (it.equals("all", ignoreCase = true) || it.equals("all otp", ignoreCase = true)) {
                    isCardOtp(messageBody, "")
                } else {
                    isCardOtp(messageBody, it)
                }
            } ?: false
            SettingType.ALL_OTP -> isMatchOtp(messageBody)
        }
    }

    private fun isMatchOtp(message: String): Boolean {
        val otpRegex = Regex("\\b\\d{4,6}\\b")
        val containsOtp = otpRegex.containsMatchIn(message)
        val containsPhrase = containsOtpPhrase(message)
        return containsOtp && containsPhrase
    }

    private fun isCardOtp(message: String, cardNumber: String): Boolean {
        val otpRegex = Regex("\\b\\d{4,6}\\b")
        val containsCardNumber = cardNumber.isEmpty() || message.contains(cardNumber, ignoreCase = true)
        val containsOtp = otpRegex.containsMatchIn(message)
        val containsPhrase = containsOtpPhrase(message)

        return containsCardNumber && containsOtp && containsPhrase
    }

    private fun containsOtpPhrase(message: String): Boolean {
        val lowerCaseMessage = message.lowercase(Locale.getDefault())
        return otpPhrases.any { phrase -> phrase in lowerCaseMessage }
    }

    private fun forwardMessage(
        context: Context, message: String, destinationNumber: String, subscriptionId: Int
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("SmsReceiver", "READ_PHONE_STATE permission not granted")
            return
        }

        val sentIntent =
            PendingIntent.getBroadcast(context, 0, Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE)
        val deliveryIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent("SMS_DELIVERED"),
            PendingIntent.FLAG_IMMUTABLE
        )

        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
        val subscriptionInfoList = subscriptionManager?.activeSubscriptionInfoList ?: listOf()

        @Suppress("DEPRECATION")
        val smsManager: SmsManager = if (subscriptionInfoList.any { info -> info.subscriptionId == subscriptionId }) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
            } else {
                SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
        }

        Log.d("SmsReceiver", "Forwarding message to $destinationNumber using subscription $subscriptionId")

        // Handle multipart messages (> 160 chars)
        if (message.length > 160) {
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(destinationNumber, null, parts, null, null)
        } else {
            smsManager.sendTextMessage(destinationNumber, null, message, sentIntent, deliveryIntent)
        }
    }
}
