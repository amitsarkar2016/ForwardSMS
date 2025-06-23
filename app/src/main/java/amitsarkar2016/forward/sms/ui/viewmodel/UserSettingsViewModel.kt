package amitsarkar2016.forward.sms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import amitsarkar2016.forward.sms.data.model.UserSettings
import amitsarkar2016.forward.sms.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserSettingsViewModel(private val repository: UserSettingsRepository) : ViewModel() {

    val userSettings: Flow<List<UserSettings>?> = repository.getUserSettings()

    fun saveUserSettings(userSettings: UserSettings) {
        viewModelScope.launch {
            repository.saveUserSettings(userSettings)
        }
    }

    fun updateUserSettings(userSettings: UserSettings) {
        viewModelScope.launch {
            repository.updateUserSettings(userSettings)
        }
    }

}