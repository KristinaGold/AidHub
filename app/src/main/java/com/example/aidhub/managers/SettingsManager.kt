package com.example.aidhub.managers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aidhub.utilities.Constants

class SettingsManager(context: Context) {

    private val appContext = context.applicationContext
    private val sharedPrefs = appContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: SettingsManager? = null

        fun init(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context).also { instance = it }
            }
        }

        fun getInstance(): SettingsManager {
            return instance ?: throw IllegalStateException(
                "SettingsManager must be initialized by calling init(context) before use."
            )
        }
    }
    fun isDarkModeEnabled(): Boolean {
        return sharedPrefs.getBoolean(Constants.DARK_MODE_KEY, false)
    }

    fun toggleDarkMode(isEnabled: Boolean) {
        sharedPrefs.edit().putBoolean(Constants.DARK_MODE_KEY, isEnabled).apply()
        applyDarkMode(isEnabled)
    }
    fun applyDarkMode(isEnabled: Boolean) {
        val mode = if (isEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }


    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(appContext).areNotificationsEnabled()
        }
    }

    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts(Constants.PACKAGE_KEY, appContext.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(intent)
    }
}