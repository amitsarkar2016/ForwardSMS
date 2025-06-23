package amitsarkar2016.forward.sms.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import amitsarkar2016.forward.sms.ui.fragment.SettingType

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: SettingType,
    val simName: String,
    val subscriptionId: String,
    val sendTo: String,
    val date: String,
    val isActive: Boolean = true,
    val data: String = "",
)
