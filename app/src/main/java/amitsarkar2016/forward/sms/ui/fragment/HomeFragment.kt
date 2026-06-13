package amitsarkar2016.forward.sms.ui.fragment

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import amitsarkar2016.forward.sms.R
import amitsarkar2016.forward.sms.data.model.SettingType
import amitsarkar2016.forward.sms.data.model.UpdateDetails
import amitsarkar2016.forward.sms.data.model.UserSettings
import amitsarkar2016.forward.sms.databinding.DialogAddRuleBinding
import amitsarkar2016.forward.sms.databinding.DialogAppUpdateBinding
import amitsarkar2016.forward.sms.databinding.FragmentHomeBinding
import amitsarkar2016.forward.sms.services.MyForegroundService
import amitsarkar2016.forward.sms.ui.extention.showToast
import amitsarkar2016.forward.sms.utils.GeneralFunctions
import amitsarkar2016.forward.sms.utils.GeneralFunctions.getAppVersion
import amitsarkar2016.forward.sms.utils.NetworkUtils
import amitsarkar2016.forward.sms.utils.SwipeToDeleteCallback
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
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) Manifest.permission.READ_PHONE_NUMBERS else null,
        if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT) Manifest.permission.POST_NOTIFICATIONS else null
    ).toTypedArray()

    private val viewModel: HomeViewModel by viewModels()
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

        val types = SettingType.entries.map { it.value }
        val adapterType = ArrayAdapter(requireContext(), R.layout.item_spinner, types)
        binding.type.setAdapter(adapterType)
        binding.type.setText(getString(R.string.match_contain), false)

        val simLabels = subscriptionInfoList.map { it.displayName.toString() }
        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, simLabels)
        binding.sim.setAdapter(adapter)

        if (subscriptionInfoList.size == 1) {
            binding.sim.setText(simLabels[0], false)
        }

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

            when (it.type) {
                SettingType.MATCH_CONTAIN -> {
                    binding.sampleSms.visibility = View.VISIBLE
                    binding.sampleSmsTitle.visibility = View.VISIBLE
                    binding.sampleSms.setHint(getString(R.string.match_contain_hint))
                }

                SettingType.ALL_SMS -> {
                    binding.sampleSms.visibility = View.GONE
                    binding.sampleSmsTitle.visibility = View.GONE
                }

                SettingType.CARD_OTP -> {
                    binding.sampleSms.visibility = View.VISIBLE
                    binding.sampleSmsTitle.visibility = View.VISIBLE
                    binding.sampleSms.setHint(getString(R.string.card_otp_hint))
                }

                SettingType.ALL_OTP -> {
                    binding.sampleSms.visibility = View.GONE
                    binding.sampleSmsTitle.visibility = View.GONE
                }
            }
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

            if (simName.isEmpty()) {
                showToast(getString(R.string.select_sim))
                return@setOnClickListener
            }
            if (sendTo.isEmpty()) {
                showToast(getString(R.string.enter_recipient))
                return@setOnClickListener
            }
            if (sendTo.length < 10) {
                showToast(getString(R.string.enter_valid_recipient))
                return@setOnClickListener
            }
            when (type) {
                SettingType.MATCH_CONTAIN.value -> {
                    if (data.isEmpty()) {
                        showToast(getString(R.string.enter_match_contain))
                        return@setOnClickListener
                    }
                    if (data.length < 3) {
                        showToast(getString(R.string.enter_valid_match_contain))
                        return@setOnClickListener
                    }
                }

                SettingType.CARD_OTP.value -> {
                    if (data.isEmpty()) {
                        showToast(getString(R.string.enter_card_number))
                        return@setOnClickListener
                    }
                    if (data.length > 6 || data.length < 4 || data.length == 5) {
                        showToast(getString(R.string.enter_valid_card_number))
                        return@setOnClickListener
                    }
                }
            }

            val userSettings = UserSettings(
                id = currentSettings?.id ?: 0,
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

    private fun startStopService(isStart: Boolean) {
        val intent = Intent(requireContext(), MyForegroundService::class.java)
        if (isStart) {
            requireContext().startService(intent)
            binding.startBtn.text = getString(R.string.stop)
        } else {
            requireContext().stopService(intent)
            binding.startBtn.text = getString(R.string.start)
        }
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
            binding.noDataFound.isVisible = it.isEmpty()
            startStopService(it.isNotEmpty())
            homeAdapter.submitList(it)
        }.launchIn(lifecycleScope)
    }

    private fun handleClicks() {
        binding.addRule.setOnClickListener {
            showAddUpdateSettingDialog()
        }
        binding.startBtn.setOnClickListener {
            // check is have and item in adapter then start service
            if (homeAdapter.itemCount == 0) {
                showToast(getString(R.string.no_rules))
                return@setOnClickListener
            }
            // check service is running or not
            val isServiceRunning = GeneralFunctions.isServiceRunning()
            startStopService(!isServiceRunning)
            if (!isServiceRunning) {
                binding.startBtn.text = getString(R.string.stop)
            } else {
                binding.startBtn.text = getString(R.string.start)
            }
        }
    }

    private fun handleSetup() {
        val isServiceRunning = GeneralFunctions.isServiceRunning()
        if (isServiceRunning) {
            binding.startBtn.text = getString(R.string.stop)
        } else {
            binding.startBtn.text = getString(R.string.start)
        }

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
            val telephonyManager = requireContext().getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
            val phoneNumber = telephonyManager.line1Number
            if (NetworkUtils.isInternetAvailable(requireContext())) {
//                viewModel.updateDevicePhoneNumber(phoneNumber)
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