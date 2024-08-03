package com.otpforward.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.otpforward.R
import com.otpforward.services.MyForegroundService
import com.otpforward.ui.extention.replaceFragmentIfNeeded
import com.otpforward.ui.fragment.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var editTextDestinationNumber: EditText
    private lateinit var spinnerSimSelection: Spinner
    private lateinit var buttonSendSms: Button
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var subscriptionInfoList: List<SubscriptionInfo>

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            setupSimSelection()
        } else {
            Toast.makeText(this, "All permissions are required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        initializeSubscriptionManager()

        if (allPermissionsGranted()) {
            setupSimSelection()
        } else {
            requestPermissionsLauncher.launch(requestSmsReadPermission)
        }

        startForegroundServiceIfNeeded()

        loadPreferences()

        buttonSendSms.setOnClickListener {
            handleSendSmsClick()
        }

        val fragment = HomeFragment()
        replaceFragmentIfNeeded(fragment, R.id.mainContainer)
    }

    private fun restartService() {
        val intent = Intent(this, MyForegroundService::class.java)
        stopService(intent)
        startService(intent)
    }

    private fun initializeViews() {
        editTextDestinationNumber = findViewById(R.id.edit_text_destination_number)
        spinnerSimSelection = findViewById(R.id.spinner_sim_selection)
        buttonSendSms = findViewById(R.id.button_send_sms)
    }

    private fun initializeSubscriptionManager() {
        subscriptionManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(SubscriptionManager::class.java)
        } else {
            getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        }
    }

    private fun startForegroundServiceIfNeeded() {
        val serviceIntent = Intent(this, MyForegroundService::class.java).apply {
            action = "com.otpforward.action.MY_SERVICE_ACTION"
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                "com.otpforward.permission.MY_SERVICE_PERMISSION"
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }

    private fun handleSendSmsClick() {
        val selectedSimPosition = spinnerSimSelection.selectedItemPosition
        val destinationNumber = editTextDestinationNumber.text.toString()

        if (selectedSimPosition != AdapterView.INVALID_POSITION && destinationNumber.isNotEmpty()) {
            val subscriptionId = subscriptionInfoList[selectedSimPosition].subscriptionId
            savePreferences(destinationNumber, subscriptionId)
            Toast.makeText(this, "SMS sent using SIM $subscriptionId", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Please select a SIM card and enter a destination number",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences("sms_preferences", Context.MODE_PRIVATE)
        val destinationNumber = sharedPreferences.getString("destination_number", "")
        val subscriptionId = sharedPreferences.getInt("subscription_id", -1)

        editTextDestinationNumber.setText(destinationNumber)

        if (subscriptionId != -1 && ::subscriptionInfoList.isInitialized) {
            val simIndex = subscriptionInfoList.indexOfFirst { it.subscriptionId == subscriptionId }
            if (simIndex != -1) {
                spinnerSimSelection.setSelection(simIndex)
            }
        }
    }

    private fun savePreferences(destinationNumber: String, subscriptionId: Int) {
        val sharedPreferences = getSharedPreferences("sms_preferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("destination_number", destinationNumber)
            putInt("subscription_id", subscriptionId)
            apply()
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return requestSmsReadPermission.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun setupSimSelection() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
        val simLabels = subscriptionInfoList.map { it.displayName.toString() }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, simLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSimSelection.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.line1Number
        }
    }

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
}
