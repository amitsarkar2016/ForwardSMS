package com.otpforward

import android.app.Application
import com.otpforward.data.local.db.AppDatabase
import com.otpforward.data.repository.UserSettingsRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application()
