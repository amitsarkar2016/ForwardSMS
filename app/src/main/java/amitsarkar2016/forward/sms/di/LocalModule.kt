package amitsarkar2016.forward.sms.di

import amitsarkar2016.forward.sms.data.local.dao.UserSettingsDao
import amitsarkar2016.forward.sms.data.local.db.AppDatabase
import amitsarkar2016.forward.sms.data.repository.UserSettingsRepository
import amitsarkar2016.forward.sms.utils.SharePrefManager
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {
    @Provides
    @Singleton
    fun provideSharePrefManager(@ApplicationContext context: Context): SharePrefManager {
        return SharePrefManager.Companion.getPrefInstance(context)
    }


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideUserSettingsDao(database: AppDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }

    @Provides
    @Singleton
    fun provideUserSettingsRepository(userSettingsDao: UserSettingsDao): UserSettingsRepository {
        return UserSettingsRepository(userSettingsDao)
    }
}