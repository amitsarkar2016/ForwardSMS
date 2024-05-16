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

/**
 * @Author: Amit Sarkar
 * @Date: 16-05-2024
 */
class SmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras
            val smsEvent = extras?.get(SmsRetriever.EXTRA_SMS_MESSAGE) as? String
            // Process the SMS event here
            processSmsEvent(context, smsEvent)
        }
    }

    private fun processSmsEvent(context: Context?, smsEvent: String?) {
        if (context == null || smsEvent == null) {
            // Error: Missing context or SMS event
            return
        }
        // Extract OTP from smsEvent and handle it
        val otp = extractOTPFromMessage(smsEvent)
        if (otp != null) {
            // OTP extraction successful, proceed with further processing
            handleOTP(otp)
        } else {
            // Error: Unable to extract OTP from SMS message
            showErrorDialog(context, "Unable to extract OTP from SMS message.")
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


    private fun handleOTP(otp: String) {
        // Validate the OTP (e.g., check if it meets length requirements, contains only digits, etc.)
        if (isValidOTP(otp)) {
            // If the OTP is valid, display it in the UI or use it for further processing
            displayOTPInUI(otp)
            // Alternatively, you can automatically submit the OTP for verification
            // submitOTPForVerification(otp)
        } else {
            // If the OTP is not valid, display an error message or take appropriate action
            showError("Invalid OTP")
        }
    }

    private fun isValidOTP(otp: String): Boolean {
        // Implement validation logic (e.g., check length, digits only, etc.)
        // For example, you can check if the OTP is exactly 6 digits long:
        return otp.length == 6 && otp.all { it.isDigit() }
    }

    private fun displayOTPInUI(otp: String) {
        // Display the OTP in the UI (e.g., set text of a TextView)
        // Example:
        // otpTextView.text = otp
        Log.e("displayOTPInUI", otp)
    }

    private fun showError(errorMessage: String) {
        // Display an error message to the user using a Toast
        Log.e("showError", errorMessage)
    }


    private fun showErrorDialog(context: Context, message: String) {
        // Display an error dialog to the user
        AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}