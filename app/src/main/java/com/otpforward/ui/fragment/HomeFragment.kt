package com.otpforward.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.otpforward.R
import com.otpforward.data.model.UpdateDetails
import com.otpforward.data.model.UserSettings
import com.otpforward.databinding.DialogAddRuleBinding
import com.otpforward.databinding.DialogAppUpdateBinding
import com.otpforward.databinding.FragmentHomeBinding
import com.otpforward.utils.GeneralFunctions
import com.otpforward.utils.GeneralFunctions.getAppVersion
import com.otpforward.utils.NetworkUtils
import com.otpforward.utils.SwipeToDeleteCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), HomeListCallBack {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var builder: AlertDialog.Builder? = null
    private var dialog: AlertDialog? = null

    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var subscriptionInfoList: List<SubscriptionInfo>

    private val requestSmsReadPermission = listOfNotNull(
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS,
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) Manifest.permission.READ_PHONE_NUMBERS else null,
        if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT) Manifest.permission.POST_NOTIFICATIONS else null
    ).toTypedArray()

    private val viewModel: HomeVieModel by viewModels()
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleSetup()
        handleClicks()
        handleViewModels()
        initializeSubscriptionManager()

        if (allPermissionsGranted()) {
            setupSimSelection()
        } else {
            requestPermissionsLauncher.launch(requestSmsReadPermission)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return requestSmsReadPermission.all {
            ContextCompat.checkSelfPermission(
                requireContext(), it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun initializeSubscriptionManager() {
        subscriptionManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireContext().getSystemService(SubscriptionManager::class.java)
        } else {
            requireContext().getSystemService(AppCompatActivity.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            setupSimSelection()
        } else {
            Toast.makeText(requireContext(), "All permissions are required", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setupSimSelection() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
        val simLabels = subscriptionInfoList.map { it.displayName.toString() }

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, simLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerSimSelection.adapter = adapter
    }

    private fun showUpdateDialog(updateDetails: UpdateDetails?) {
        builder = AlertDialog.Builder(requireContext())
        val binding = DialogAppUpdateBinding.inflate(LayoutInflater.from(requireContext()))
        builder?.setView(binding.root)
        dialog = builder?.create()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.cancelButton.setOnClickListener {
            requireActivity().finish()
        }
        binding.updateButton.setOnClickListener {
            downloadApk(updateDetails?.url)
        }
        dialog?.show()
    }

    private fun downloadApk(url: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun showAddUpdateSettingDialog(currentSettings: UserSettings? = null) {
        val builder = AlertDialog.Builder(requireContext())
        val binding = DialogAddRuleBinding.inflate(LayoutInflater.from(requireContext()))
        builder.setView(binding.root)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val simLabels = subscriptionInfoList.map { it.displayName.toString() }
        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, simLabels)
        binding.sim.setAdapter(adapter)

        val types = SettingType.entries.map { it.value }
        val adapterType = ArrayAdapter(requireContext(), R.layout.item_spinner, types)
        binding.type.setAdapter(adapterType)
        binding.type.setText(getString(R.string.match_contain), false)

        binding.type.setOnClickListener {
            binding.type.showDropDown()
        }

        binding.type.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    binding.sampleSms.visibility = View.VISIBLE
                    binding.sampleSmsTitle.visibility = View.VISIBLE
                    binding.sampleSms.setHint(getString(R.string.match_contain_hint))
                }

                1 -> {
                    binding.sampleSms.visibility = View.GONE
                    binding.sampleSmsTitle.visibility = View.GONE
                }

                2 -> {
                    binding.sampleSms.visibility = View.VISIBLE
                    binding.sampleSmsTitle.visibility = View.VISIBLE
                    binding.sampleSms.setHint(getString(R.string.card_otp_hint))
                }

                3 -> {
                    binding.sampleSms.visibility = View.GONE
                    binding.sampleSmsTitle.visibility = View.GONE
                }
            }
        }

        currentSettings?.let {
            binding.type.setText(it.type.value, false)
            binding.sim.setText(it.simName)
            binding.recipient.setText(it.sendTo)
            binding.sampleSms.setText(it.data)
            binding.addUpdate.text = getString(R.string.update)
        }

        binding.sim.setOnClickListener {
            binding.sim.showDropDown()
        }

        binding.addUpdate.setOnClickListener {
            val type = binding.type.text.toString()
            val simName = binding.sim.text.toString()
            val subscriptionId =
                subscriptionInfoList.find { it.displayName.toString() == simName }?.subscriptionId?.toString()
                    ?: ""
            val sendTo = binding.recipient.text.toString()
            val data = binding.sampleSms.text.toString()
            val date = getCurrentDate()

            val userSettings = UserSettings(
                type = SettingType.entries.find { it.value == type } ?: SettingType.MATCH_CONTAIN,
                simName = simName,
                subscriptionId = subscriptionId,
                sendTo = sendTo,
                date = date,
                data = data
            )

            if (currentSettings != null) {
                viewModel.updateUserSettings(userSettings)
            } else {
                viewModel.saveUserSettings(userSettings)
            }
            dialog.dismiss()
        }

        binding.close.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }


    private fun handleViewModels() {
        viewModel.updateAvailable.onEach {
            if (it.status) {
                showUpdateDialog(it.data)
            }

        }.launchIn(lifecycleScope)

        viewModel.userSettings.onEach {
            homeAdapter.submitList(it)
        }.launchIn(lifecycleScope)
    }

    private fun handleClicks() {
        binding.addRule.setOnClickListener {
            showAddUpdateSettingDialog()
        }
    }

    private fun handleSetup() {
        homeAdapter = HomeAdapter(this)
        binding.recyclerView.adapter = homeAdapter

        val swipeHandler = SwipeToDeleteCallback(requireContext(), ::onDelete, homeAdapter)
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    override fun onResume() {
        super.onResume()

        if (NetworkUtils.isInternetAvailable(requireContext())) {
            viewModel.checkUpdateAvailable(getAppVersion(requireContext()))
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_PHONE_NUMBERS
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val telephonyManager =
                requireContext().getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
            val phoneNumber = telephonyManager.line1Number
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                viewModel.updateDevicePhoneNumber(phoneNumber)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onItemClick(item: UserSettings) {
        showAddUpdateSettingDialog(item)
    }
    override fun onDelete(item: UserSettings) {
        GeneralFunctions.showDeleteConfirmation(requireContext()) {
            viewModel.deleteUserSettings(item.id)
        }
    }
}