package amitsarkar2016.forward.sms.data.remote

import amitsarkar2016.forward.sms.data.model.BaseResponse
import amitsarkar2016.forward.sms.data.model.Login
import amitsarkar2016.forward.sms.data.model.UpdateDetails
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST(UrlHelper.LOGIN)
    suspend fun login(@FieldMap hashMap: HashMap<String, String>): Response<BaseResponse<Login>>

    @FormUrlEncoded
    @POST(UrlHelper.DEVICE_PHONE_NUMBER)
    suspend fun devicePhoneNumber(@FieldMap hashMap: HashMap<String, String>): Response<BaseResponse<Unit>>

    @FormUrlEncoded
    @POST(UrlHelper.UPDATE_AVAILABILITY)
    suspend fun checkUpdateAvailable(@FieldMap hashMap: HashMap<String, String>): Response<BaseResponse<UpdateDetails>>


}