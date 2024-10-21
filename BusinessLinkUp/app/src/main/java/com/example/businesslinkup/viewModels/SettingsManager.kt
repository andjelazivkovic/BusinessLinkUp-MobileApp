package com.example.businesslinkup.viewModels

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_RADIUS = "radius"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    fun getRadius(): Int {
        return sharedPreferences.getInt(KEY_RADIUS, 2000)
    }

    fun saveRadius(radius: Int) {
        sharedPreferences.edit().putInt(KEY_RADIUS, radius).apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
}
