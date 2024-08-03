package com.otpforward.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.otpforward.R
import com.otpforward.databinding.ActivityOnBoardingBinding
import com.otpforward.ui.extention.hideSystemUI
import com.otpforward.utils.SharePrefManager


class OnBoardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnBoardingBinding

    companion object {
        private val requestSmsReadPermission = listOfNotNull(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) Manifest.permission.READ_PHONE_NUMBERS else null,
            if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT) Manifest.permission.POST_NOTIFICATIONS else null
        ).toTypedArray()
    }

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {

                val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
                val phoneNumber = telephonyManager.line1Number
                if (phoneNumber != null && phoneNumber.isNotEmpty()) {
                    Log.d("Phone Number", phoneNumber)
                } else {
                    Log.d("Phone Number", "Phone number is not available")
                }

                SharePrefManager.getPrefInstance(this).saveBoolean("isOnBoardingDone", true)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemUI()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnGetStarted.setOnClickListener {
            requestPermissionLauncher.launch(requestSmsReadPermission)
        }

    }
}