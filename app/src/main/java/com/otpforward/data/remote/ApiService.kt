package com.otpforward.data.remote

import com.otpforward.data.model.BaseResponse
import com.otpforward.data.model.Login
import com.otpforward.data.remote.UrlHelper
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST(UrlHelper.LOGIN)
    suspend fun login(@FieldMap hashMap: HashMap<String, String>): Response<BaseResponse<Login>>

}