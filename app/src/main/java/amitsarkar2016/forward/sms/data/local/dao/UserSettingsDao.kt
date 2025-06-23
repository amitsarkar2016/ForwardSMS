package amitsarkar2016.forward.sms.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import amitsarkar2016.forward.sms.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userSettings: UserSettings)

    @Query("SELECT * FROM user_settings")
    fun getUserSettings(): Flow<List<UserSettings>>

    @Update
    suspend fun updateUserSettings(userSettings: UserSettings): Int

    @Query("UPDATE user_settings SET data = :data")
    suspend fun updateUserSettings(data: String)

    @Query("DELETE FROM user_settings")
    suspend fun deleteAll()

    @Query("DELETE FROM user_settings WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM user_settings WHERE id = :id")
    fun getUserSettingById(id: Int): Flow<UserSettings?>
}
