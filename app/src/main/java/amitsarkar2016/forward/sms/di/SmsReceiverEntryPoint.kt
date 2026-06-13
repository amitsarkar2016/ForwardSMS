package amitsarkar2016.forward.sms.di

import amitsarkar2016.forward.sms.data.repository.UserSettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SmsReceiverEntryPoint {
    fun userSettingsRepository(): UserSettingsRepository
}
