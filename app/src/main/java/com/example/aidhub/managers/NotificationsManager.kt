package com.example.aidhub.managers

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.aidhub.utilities.Constants
import com.google.firebase.messaging.FirebaseMessaging

class NotificationsManager(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val notificationPermission = Manifest.permission.POST_NOTIFICATIONS

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: NotificationsManager? = null

        fun init(context: Context): NotificationsManager {
            return instance ?: synchronized(this) {
                instance ?: NotificationsManager(context).also { instance = it }
            }
        }

        fun getInstance(): NotificationsManager {
            return instance ?: throw IllegalStateException(
                "NotificationManager must be initialized by calling init(context) before use."
            )
        }
    }


    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                context,
                notificationPermission
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun createNotificationChannel() {
        if (hasNotificationPermission()) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                "AidHub Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications from AidHub about new requests opened."
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val notificationManager =
                ContextCompat.getSystemService(context, NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun getToken(onResult: (String) -> Unit){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(task.result)
            }
        }
    }

}