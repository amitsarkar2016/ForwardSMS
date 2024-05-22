package com.otpforward

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
//        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        editTextDestinationNumber = findViewById(R.id.edit_text_destination_number)
        spinnerSimSelection = findViewById(R.id.spinner_sim_selection)
        buttonSendSms = findViewById(R.id.button_send_sms)

        subscriptionManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(SubscriptionManager::class.java)
        } else {
            getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        }

        if (allPermissionsGranted()) {
            setupSimSelection()
        } else {
            requestPermissionsLauncher.launch(requestSmsReadPermission)
        }

        loadPreferences()

        buttonSendSms.setOnClickListener {
            val selectedSimPosition = spinnerSimSelection.selectedItemPosition
            val destinationNumber = editTextDestinationNumber.text.toString()

            if (selectedSimPosition != AdapterView.INVALID_POSITION && destinationNumber.isNotEmpty()) {
                val subscriptionId = subscriptionInfoList[selectedSimPosition].subscriptionId
                savePreferences(destinationNumber, subscriptionId)
            } else {
                Toast.makeText(this, "Please select a SIM card and enter a destination number", Toast.LENGTH_SHORT).show()
            }
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
        val simLabels = subscriptionInfoList.map { it.displayName.toString() }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, simLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSimSelection.adapter = adapter
    }

    companion object {
        private val requestSmsReadPermission = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
    }
}
