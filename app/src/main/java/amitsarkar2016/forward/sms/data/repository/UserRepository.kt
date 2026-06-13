package amitsarkar2016.forward.sms.data.repository

import amitsarkar2016.forward.sms.core.Constant.KEY_TOKEN
import amitsarkar2016.forward.sms.data.model.BaseResponse
import amitsarkar2016.forward.sms.data.model.Login
import amitsarkar2016.forward.sms.data.model.UpdateDetails
import amitsarkar2016.forward.sms.data.remote.ApiService
import amitsarkar2016.forward.sms.utils.SharePrefManager
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

}
