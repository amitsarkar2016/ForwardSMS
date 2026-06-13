package amitsarkar2016.forward.sms.ui.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import amitsarkar2016.forward.sms.data.model.BaseResponse
import amitsarkar2016.forward.sms.data.model.UpdateDetails
import amitsarkar2016.forward.sms.data.model.UserSettings
import amitsarkar2016.forward.sms.data.repository.UserRepository
import amitsarkar2016.forward.sms.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userSettingsRepository: UserSettingsRepository,
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

    val userSettings: Flow<List<UserSettings>> = userSettingsRepository.getUserSettings()

    fun saveUserSettings(userSettings: UserSettings) {
        viewModelScope.launch {
            userSettingsRepository.saveUserSettings(userSettings)
        }
    }

    fun updateUserSettings(userSettings: UserSettings) {
        viewModelScope.launch {
            userSettingsRepository.updateUserSettings(userSettings)
        }
    }
    fun deleteUserSettings(id: Int) {
        viewModelScope.launch {
            userSettingsRepository.deleteUserSettings(id)
        }
    }
}