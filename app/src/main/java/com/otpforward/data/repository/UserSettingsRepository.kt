package com.otpforward.data.repository

import android.content.Context
import com.otpforward.data.local.dao.UserSettingsDao
import com.otpforward.data.local.db.AppDatabase
import com.otpforward.data.model.UserSettings
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

    companion object {
        @Volatile
        private var INSTANCE: UserSettingsRepository? = null

        fun getInstance(context: Context): UserSettingsRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserSettingsRepository(AppDatabase.getDatabase(context).userSettingsDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
