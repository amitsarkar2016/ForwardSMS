package com.otpforward.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otpforward.data.model.UserSettings
import com.otpforward.data.repository.UserSettingsRepository
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