package amitsarkar2016.forward.sms.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import amitsarkar2016.forward.sms.data.model.BaseResponse
import amitsarkar2016.forward.sms.data.remote.UrlHelper.BASE_URL
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
import java.lang.reflect.Type
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
//                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Content-Type", "application/json")
                .header("token", tokens)
                .header("jwt_token", tokens)

            val request = requestBuilder.build()

            try {
                // Proceed with the request
                val response = chain.proceed(request)

                // Read and handle the raw response body
                val responseBody = response.body?.string()
                if (responseBody != null && responseBody.startsWith("{")) {
                    // If the response is valid JSON, rebuild it
                    return@addInterceptor response.newBuilder()
                        .body(responseBody.toResponseBody(response.body?.contentType()))
                        .build()
                } else {
                    // Log and throw an exception for non-JSON responses
                    Log.e("Interceptor", "Invalid response: $responseBody")
                    throw IOException("Non-JSON response received")
                }
            } catch (exception: Exception) {
                // Log and handle the exception
                exception.printStackTrace()
                return@addInterceptor handleException(exception, originalRequest)
            }
        }

        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create(gson))
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

class StringOrObjectAdapter : JsonDeserializer<Any> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Any {
        return if (json.isJsonObject) {
            context.deserialize(json, typeOfT)
        } else {
            json.asString
        }
    }
}