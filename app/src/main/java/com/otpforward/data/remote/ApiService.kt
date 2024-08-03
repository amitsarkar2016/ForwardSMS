package com.otpforward.data.remote

import com.otpforward.data.model.BaseResponse
import com.otpforward.data.model.Contact
import com.otpforward.data.model.Login
import com.otpforward.data.model.UpdateDetails
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.ArrayList

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

    @POST(UrlHelper.SYNC_CONTACTS)
    suspend fun syncContacts(@Body contactsList: ArrayList<Contact>): Response<BaseResponse<Unit>>

}