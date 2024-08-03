package com.otpforward.ui.fragment

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otpforward.data.model.BaseResponse
import com.otpforward.data.model.Contact
import com.otpforward.data.model.UpdateDetails
import com.otpforward.data.model.UserSettings
import com.otpforward.data.repository.UserRepository
import com.otpforward.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeVieModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userSettingsRepository: UserSettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    fun updateDevicePhoneNumber(phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updateDevicePhoneNumber(phoneNumber)
        }
    }

    val updateAvailable = MutableSharedFlow<BaseResponse<UpdateDetails>>()

    fun checkUpdateAvailable(version: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateAvailable.emit(userRepository.checkUpdateAvailable(version))
        }
    }

    private fun fetchAndDisplayContacts() {
        try {
            val contactsList = ArrayList<Contact>()
            val phoneNumbersSet = HashSet<String>()

            val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ), null, null, "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use { phoneCursor ->
                while (phoneCursor.moveToNext()) {
                    val contactName = phoneCursor.getString(0)
                    val phoneNumber =
                        phoneCursor.getString(1).replace("^\\+91|\\s+|\\D+".toRegex(), "")
                    if (phoneNumber.length == 10 && phoneNumbersSet.add(phoneNumber)) {
                        contactsList.add(Contact(contactName, phoneNumber))
                    }
                }
            }

            cursor?.close()
            // sync with web
            viewModelScope.launch(Dispatchers.IO) {
                userRepository.syncContacts(contactsList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val userSettings: Flow<List<UserSettings>> = userSettingsRepository.getUserSettings()

    fun saveUserSettings(userSettings: UserSettings) {
        viewModelScope.launch {
            userSettingsRepository.saveUserSettings(userSettings)
            userSettingsRepository.getUserSettings()
        }
    }

    fun updateUserSettings(userSettings: UserSettings) {
        viewModelScope.launch {
            userSettingsRepository.updateUserSettings(userSettings)
            userSettingsRepository.getUserSettings()
        }
    }
    fun deleteUserSettings(id: Int) {
        viewModelScope.launch {
            userSettingsRepository.deleteUserSettings(id)
            userSettingsRepository.getUserSettings()
        }
    }

    init {
        fetchAndDisplayContacts()
    }
}