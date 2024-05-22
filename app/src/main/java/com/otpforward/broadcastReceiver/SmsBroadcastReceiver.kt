package com.otpforward.broadcastReceiver

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.phone.SmsRetriever
import java.util.regex.Pattern

class SmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras
            val smsEvent = extras?.get(SmsRetriever.EXTRA_SMS_MESSAGE) as? String
            processSmsEvent(context, smsEvent)
        }
    }

    private fun processSmsEvent(context: Context?, smsEvent: String?) {
        if (context == null || smsEvent == null) {
            return
        }
        val otp = extractOTPFromMessage(smsEvent)
        if (otp != null) {
            handleOTP(otp)
        } else {
            showErrorDialog(context, "Unable to extract OTP from SMS message.")
        }
    }

    private fun extractOTPFromMessage(smsEvent: String): String? {
        val otpPattern = "\\b\\d{6}\\b".toRegex()
        val matchResult = otpPattern.find(smsEvent)
        return matchResult?.value
    }


    private fun handleOTP(otp: String) {
        if (isValidOTP(otp)) {
            displayOTPInUI(otp)
        } else {
            showError("Invalid OTP")
        }
    }

    private fun isValidOTP(otp: String): Boolean {
        return otp.length == 6 && otp.all { it.isDigit() }
    }

    private fun displayOTPInUI(otp: String) {
        Log.e("displayOTPInUI", otp)
    }

    private fun showError(errorMessage: String) {
        Log.e("showError", errorMessage)
    }


    private fun showErrorDialog(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}