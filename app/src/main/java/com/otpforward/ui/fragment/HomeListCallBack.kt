package com.otpforward.ui.fragment

import com.otpforward.data.model.UserSettings

interface HomeListCallBack {
    fun onItemClick(item: UserSettings)
    fun onDelete(item: UserSettings)
}