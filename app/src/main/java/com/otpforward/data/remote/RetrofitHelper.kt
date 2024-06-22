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

object RetrofitHelper {

    fun getInstance(context: Context): Retrofit {
        val tokens = SharePrefManager.getPrefInstance(context).getString(Constant.KEY_TOKEN).toString()
        Log.e("tokens", tokens)

        val httpLoggingInterceptor =
            HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY)
                .setLevel(HttpLoggingInterceptor.Level.HEADERS)

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
                .header("token", tokens)
                .header("jwt_token", tokens)

            val request = requestBuilder.build()

            try {
                return@addInterceptor chain.proceed(request)
            } catch (exception: Exception) {
                when (exception) {
                    is SocketTimeoutException -> {
                        Log.e("NetworkInterceptor", "SocketTimeoutException: ${exception.message}")
                    }

                    is SocketException -> {
                        Log.e("NetworkInterceptor", "SocketException: ${exception.message}")
                    }

                    is IOException -> {
                        Log.e("NetworkInterceptor", "IOException: ${exception.message}")
                    }

                    else -> {
                        Log.e("NetworkInterceptor", "Exception: ${exception.message}")
                    }
                }
                val customError = BaseResponse<String>(false, exception.message ?: "Unknown error")
                val customContent = Gson().toJson(customError)

                return@addInterceptor Response.Builder().request(originalRequest)
                    .protocol(Protocol.HTTP_1_1).code(200).message("").body(
                        customContent.toResponseBody("application/json".toMediaTypeOrNull())
                    ).build()
            }
        }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String,
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String,
                    ) {
                    }

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