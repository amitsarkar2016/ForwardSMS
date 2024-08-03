package com.otpforward.provides

import android.content.Context
import androidx.room.Room
import com.otpforward.core.Constant
import com.otpforward.data.local.dao.UserSettingsDao
import com.otpforward.data.local.db.AppDatabase
import com.otpforward.data.remote.ApiService
import com.otpforward.data.remote.RetrofitHelper
import com.otpforward.data.repository.UserRepository
import com.otpforward.data.repository.UserSettingsRepository
import com.otpforward.utils.SharePrefManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSharePrefManager(@ApplicationContext context: Context): SharePrefManager {
        return SharePrefManager.getPrefInstance(context)
    }

    @Provides
    @Singleton
    fun provideRetrofit(@ApplicationContext context: Context, sharePrefManager: SharePrefManager): Retrofit {
        val tokens = sharePrefManager.getString(Constant.KEY_TOKEN) ?: ""
        return RetrofitHelper.getInstance(tokens, context)
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService, sharePrefManager: SharePrefManager): UserRepository {
        return UserRepository(apiService, sharePrefManager)
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
