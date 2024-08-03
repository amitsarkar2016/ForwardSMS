package com.otpforward.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.otpforward.core.Constant
import com.otpforward.data.model.BaseResponse
import com.otpforward.data.remote.UrlHelper.BASE_URL
import com.otpforward.utils.SharePrefManager
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// RetrofitHelper.kt
object RetrofitHelper {
    fun getInstance(tokens: String, context: Context): Retrofit {
        val httpLoggingInterceptor =
            HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY)

        val httpClient = getUnsafeOkHttpClient()
            .cache(Cache(context.applicationContext.cacheDir, 100 * 1024 * 1024))
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        httpClient.addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Content-Type", "application/json")
                .header("token", tokens)
                .header("jwt_token", tokens)

            val request = requestBuilder.build()

            try {
                return@addInterceptor chain.proceed(request)
            } catch (exception: Exception) {
                handleException(exception, originalRequest)
            }
        }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun handleException(exception: Exception, originalRequest: okhttp3.Request): Response {
        val customError = BaseResponse<String>(false, exception.message ?: "Unknown error")
        val customContent = Gson().toJson(customError)

        return Response.Builder().request(originalRequest)
            .protocol(Protocol.HTTP_1_1).code(200).message("").body(
                customContent.toResponseBody("application/json".toMediaTypeOrNull())
            ).build()
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
            builder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}