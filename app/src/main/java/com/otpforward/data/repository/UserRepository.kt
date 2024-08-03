package com.otpforward.data.repository

import com.otpforward.core.Constant.KEY_TOKEN
import com.otpforward.data.model.BaseResponse
import com.otpforward.data.model.Contact
import com.otpforward.data.model.Login
import com.otpforward.data.model.UpdateDetails
import com.otpforward.data.remote.ApiService
import com.otpforward.utils.SharePrefManager
import java.util.ArrayList
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val sharePrefManager: SharePrefManager,
) {
    suspend fun login(username: String, password: String): BaseResponse<Login>? {
        val response = apiService.login(
            hashMapOf(
                "user" to username,
                "password" to password,
            )
        )
        if (response.isSuccessful && response.body()?.status == true) {
            response.body()?.data?.jwt_token?.let { saveToken(it) }
        }
        return response.body()
    }

    private fun saveToken(token: String) {
        sharePrefManager.saveString(KEY_TOKEN, token)
    }

    suspend fun updateDevicePhoneNumber(phoneNumber: String) {
        apiService.devicePhoneNumber(hashMapOf("phone_number" to phoneNumber))
    }

    suspend fun checkUpdateAvailable(version: String): BaseResponse<UpdateDetails> {
        val result = apiService.checkUpdateAvailable(hashMapOf("version" to version))
        return if (result.isSuccessful) {
            result.body() ?: BaseResponse(false, "Something went wrong", null)
        } else {
            BaseResponse(false, "Something went wrong", null)
        }
    }

    suspend fun syncContacts(contactsList: ArrayList<Contact>) {
        apiService.syncContacts(contactsList)
    }
}
