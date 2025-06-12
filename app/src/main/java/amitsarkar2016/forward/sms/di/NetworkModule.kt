package amitsarkar2016.forward.sms.di

import amitsarkar2016.forward.sms.core.Constant
import amitsarkar2016.forward.sms.data.remote.ApiService
import amitsarkar2016.forward.sms.data.remote.RetrofitHelper
import amitsarkar2016.forward.sms.data.repository.UserRepository
import amitsarkar2016.forward.sms.utils.SharePrefManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://your.api.base.url/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
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
}
