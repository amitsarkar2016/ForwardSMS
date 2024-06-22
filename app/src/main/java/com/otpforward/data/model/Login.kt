package com.otpforward.data.model

data class Login(
    val status: Boolean,
    val msg: String,
    val token: String,
    val jwt_token: String,
)
