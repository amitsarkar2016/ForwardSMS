package com.otpforward.data.repository

import com.otpforward.core.Constant.KEY_TOKEN
import com.otpforward.data.model.BaseResponse
import com.otpforward.data.model.Login
import com.otpforward.data.remote.ApiService
import com.otpforward.utils.SharePrefManager
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
}
