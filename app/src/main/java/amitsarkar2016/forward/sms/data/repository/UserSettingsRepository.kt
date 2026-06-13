package amitsarkar2016.forward.sms.data.repository

import amitsarkar2016.forward.sms.data.local.dao.UserSettingsDao
import amitsarkar2016.forward.sms.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserSettingsRepository @Inject constructor(private val userSettingsDao: UserSettingsDao) {

    fun getUserSettings(): Flow<List<UserSettings>> {
        return userSettingsDao.getUserSettings()
    }

    suspend fun updateUserSettings(userSettings: UserSettings): Int {
        return userSettingsDao.updateUserSettings(userSettings)
    }

    suspend fun saveUserSettings(userSettings: UserSettings) {
        userSettingsDao.insert(userSettings)
    }

    suspend fun deleteUserSettings(id: Int) {
        userSettingsDao.deleteById(id)
    }
}
