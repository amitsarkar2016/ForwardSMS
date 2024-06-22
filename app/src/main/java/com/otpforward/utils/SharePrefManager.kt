package com.otpforward.utils

import android.content.Context
import android.content.SharedPreferences

class SharePrefManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        context.packageName,
        Context.MODE_PRIVATE
    )

    private val editor: SharedPreferences.Editor
        get() = sharedPreferences.edit()

    fun getString(key: String, default: String? = null): String? {
        return sharedPreferences.getString(key, default)
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    fun saveString(key: String, value: String) {
        editor.putString(key, value).apply()
    }

    fun saveBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun clearData() {
        editor.clear().apply()
    }

    fun logout() {
        clearData()
    }

    companion object {
        @Volatile
        private var INSTANCE: SharePrefManager? = null

        fun getPrefInstance(context: Context): SharePrefManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharePrefManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}