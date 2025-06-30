package amitsarkar2016.forward.sms.data.model

data class BaseResponse<T>(
    val status: Boolean,
    val msg: String,
    val data: T? = null,
)