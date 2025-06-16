package amitsarkar2016.forward.sms.ui.fragment

import amitsarkar2016.forward.sms.data.model.UserSettings

interface HomeListCallBack {
    fun onItemClick(item: UserSettings)
    fun onDelete(item: UserSettings)
}