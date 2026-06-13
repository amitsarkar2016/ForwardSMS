package amitsarkar2016.forward.sms.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import amitsarkar2016.forward.sms.data.local.dao.UserSettingsDao
import amitsarkar2016.forward.sms.data.model.UserSettings

@Database(entities = [UserSettings::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userSettingsDao(): UserSettingsDao
}
