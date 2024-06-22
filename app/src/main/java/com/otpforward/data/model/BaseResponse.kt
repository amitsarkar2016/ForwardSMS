package com.otpforward.data.model

data class BaseResponse<T>(
    val status: Boolean,
    val msg: String,
    val data: T? = null,
)